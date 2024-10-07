package net.perfectdreams.loritta.lorituber.server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.*
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.recipes.LoriTuberRecipes
import net.perfectdreams.loritta.lorituber.rpc.packets.*
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehaviors
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.processors.*
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.data.*
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberVideo
import net.perfectdreams.loritta.lorituber.server.state.items.LoriTuberGroceryItem
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LoriTuberServer(
    private val lorituberDatabase: Database,
    val gameState: GameState
) {
    companion object {
        // For comparisons:
        // The Sims 1: one in game minute = one real life second
        // The Sims Online: one in game minute = five real life seconds
        const val TICKS_PER_SECOND = 1
        const val TICK_DELAY = 1_000
        const val ONE_HOUR_IN_TICKS = 60
        const val ONE_DAY_IN_TICKS = 1_440
        private val logger = KotlinLogging.logger {}
        const val GENERAL_INFO_KEY = "general"

        private val NELSON_GROCERY_STORE_ITEMS = listOf(
            LoriTuberItems.SLICED_BREAD,
            LoriTuberItems.GESSY_CEREAL,
            LoriTuberItems.MILK,
            LoriTuberItems.STRAWBERRY_YOGURT
        )
    }

    // var currentTick = 0L
    // var lastUpdate = System.currentTimeMillis()
    val averageTickDurations = mutableListOf<Duration>()
    private val tickingMutex = Mutex()

    private val getWorldInfoProcessor = GetWorldInfoProcessor(this)
    private val getCharactersByOwnerRequest = GetCharactersByOwnerProcessor(this)
    private val createCharacterProcessor = CreateCharacterProcessor(this)
    private val viewCharacterMotivesProcessor = ViewCharacterMotivesProcessor(this)
    private val setCharacterMotivesProcessor = SetCharacterMotivesProcessor(this)
    private val setCharacterSleepingProcessor = SetCharacterSleepingProcessor(this)
    private val goToGroceryStoreResponse = GoToGroceryStoreProcessor(this)
    private val buyGroceryStoreItemProcessor = BuyGroceryStoreItemProcessor(this)
    private val prepareCraftingProcessor = PrepareCraftingProcessor(this)
    private val startCraftingProcessor = StartCraftingProcessor(this)
    private val selectFoodMenuProcessor = SelectFoodMenuProcessor(this)
    private val eatFoodProcessor = EatFoodProcessor(this)
    private val getChannelsByCharacterProcessor = GetChannelsByCharacterProcessor(this)
    private val createChannelProcessor = CreateChannelProcessor(this)
    private val getChannelByIdProcessor = GetChannelByIdProcessor(this)
    private val createPendingVideoProcessor = CreatePendingVideoProcessor(this)
    private val getPendingVideoByIdProcessor = GetPendingVideoByIdProcessor(this)
    private val startWorkingOnPendingVideoProcessor = StartWorkingOnPendingVideoProcessor(this)
    private val finishPendingVideoProcessor = FinishPendingVideoProcessor(this)
    private val getChannelVideosProcessor = GetChannelVideosProcessor(this)
    private val answerPhoneProcessor = AnswerPhoneProcessor(this)
    private val takingAShowerProcessor = SetCharacterTakingAShowerProcessor(this)
    private val usingToiletProcessor = SetCharacterUsingToiletProcessor(this)
    private val characterUseItemProcessor = CharacterUseItemProcessor(this)

    private var isFirstTick = false
    private var cantKeepUp = false

    // TODO: Move this somewhere else
    // Made by ChatGPT
    fun <T> List<T>.combinations(length: Int): List<List<T>> {
        if (length == 0) return listOf(emptyList())
        if (this.isEmpty()) return emptyList()

        val head = first()
        val tail = drop(1)

        val combWithHead = tail.combinations(length - 1).map { listOf(head) + it }
        val combWithoutHead = tail.combinations(length)

        return combWithHead + combWithoutHead
    }

    fun start() {
        // TODO: Remove this (actually just move this somewhere else)
        gameState.lotsById[SpecialLots.NELSON_GROCERY_STORE] = LoriTuberLot(
            SpecialLots.NELSON_GROCERY_STORE,
            LoriTuberLotData(
                LotType.Community,
                listOf(
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.ITEM_STORE.id,
                        1,
                        null
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHARACTER_PORTAL.id,
                        1,
                        null
                    )
                )
            )
        )

        gameState.lotsById[SpecialLots.OUTSIDE] = LoriTuberLot(
            SpecialLots.OUTSIDE,
            LoriTuberLotData(
                LotType.Community,
                listOf(
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHARACTER_PORTAL.id,
                        1,
                        null
                    )
                )
            )
        )

        gameState.lotsById[SpecialLots.STARRY_BEACH] = LoriTuberLot(
            SpecialLots.STARRY_BEACH,
            LoriTuberLotData(
                LotType.Community,
                listOf(
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.BEACH_OCEAN.id,
                        1,
                        null
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHARACTER_PORTAL.id,
                        1,
                        null
                    )
                )
            )
        )

        val bools = listOf(false, true)

        for (category in LoriTuberVideoContentCategory.entries) {
            val randomVibes = LoriTuberVibes(0)
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE1, bools.random())
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE2, bools.random())
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE3, bools.random())
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE4, bools.random())
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE5, bools.random())
            randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE6, bools.random())
            // randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE7, bools.random())

            gameState.trendsByCategory[category] = LoriTuberTrendData(randomVibes)
        }

        val allPossibleCategoryCombinations = LoriTuberVideoContentCategory.entries.combinations(3)

        // In total there is 2304 possible combinations for each category

        var x = 0L
        for (categories in allPossibleCategoryCombinations) {
            for (vibe1 in bools) {
                for (vibe2 in bools) {
                    for (vibe3 in bools) {
                        for (vibe4 in bools) {
                            for (vibe5 in bools) {
                                for (vibe6 in bools) {
                                    for (vibe7 in bools) {
                                        val vibes = LoriTuberVibes(0)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE1, vibe1)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE2, vibe2)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE3, vibe3)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE4, vibe4)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE5, vibe5)
                                        vibes.setVibe(LoriTuberVideoContentVibes.VIBE6, vibe6)
                                        // vibes.setVibe(LoriTuberVideoContentVibes.VIBE7, vibe7)

                                        val superViewerId = x++
                                        /* gameState.superViewersById[superViewerId] = LoriTuberSuperViewer(
                                            superViewerId,
                                            LoriTuberSuperViewerData(
                                                categories,
                                                vibes,
                                                // It seems that 10 allocated engagement is a "healthy" default instead of 1
                                                10,
                                            )
                                        ) */
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // logger.info { "Total SuperViewers: ${gameState.superViewersById.size}" }

        // This is the LoriTuber server endpoint
        embeddedServer(CIO, port = 3001) {
            routing {
                post("/rpc") {
                    val request = Json.decodeFromString<LoriTuberRequest>(call.receiveText())

                    val response = tickingMutex.withLock {
                        when (request) {
                            is GetWorldInfoRequest -> getWorldInfoProcessor.process(request)
                            is CreateCharacterRequest -> createCharacterProcessor.process(request)
                            is GetCharactersByOwnerRequest -> getCharactersByOwnerRequest.process(request)
                            is ViewCharacterMotivesRequest -> viewCharacterMotivesProcessor.process(request)
                            is SetCharacterMotivesRequest -> setCharacterMotivesProcessor.process(request)
                            is SetCharacterSleepingRequest -> setCharacterSleepingProcessor.process(request)
                            is GoToGroceryStoreRequest -> goToGroceryStoreResponse.process(request)
                            is BuyGroceryStoreItemRequest -> buyGroceryStoreItemProcessor.process(request)
                            is PrepareCraftingRequest -> prepareCraftingProcessor.process(request)
                            is StartCraftingRequest -> startCraftingProcessor.process(request)
                            is SelectFoodMenuRequest -> selectFoodMenuProcessor.process(request)
                            is EatFoodRequest -> eatFoodProcessor.process(request)
                            is GetChannelsByCharacterRequest -> getChannelsByCharacterProcessor.process(request)
                            is CreateChannelRequest -> createChannelProcessor.process(request)
                            is GetChannelByIdRequest -> getChannelByIdProcessor.process(request)
                            is CreatePendingVideoRequest -> createPendingVideoProcessor.process(request)
                            is GetPendingVideoByIdRequest -> getPendingVideoByIdProcessor.process(request)
                            is StartWorkingOnPendingVideoRequest -> startWorkingOnPendingVideoProcessor.process(request)
                            is FinishPendingVideoRequest -> finishPendingVideoProcessor.process(request)
                            is GetChannelVideosRequest -> getChannelVideosProcessor.process(request)
                            is AnswerPhoneRequest -> answerPhoneProcessor.process(request)
                            is SetCharacterTakingAShowerRequest -> takingAShowerProcessor.process(request)
                            is SetCharacterUsingToiletRequest -> usingToiletProcessor.process(request)
                            is CharacterUseItemRequest -> characterUseItemProcessor.process(request)
                        }
                    }

                    call.respondText(
                        Json.encodeToString<LoriTuberResponse>(response)
                    )
                }
            }
        }.start(wait = false)

        thread(name = "LoriTuber Game Loop") {
            gameLoop()
        }

        while (true) {
            val line = readln()
            runBlocking {
                tickingMutex.withLock {
                    try {
                        if (line == "spawn_video_new") {
                            val channel = gameState.channels.first()

                            val videoEvents = mutableMapOf<Long, List<LoriTuberVideoEvent>>()

                            val video = LoriTuberVideo(
                                gameState.generateVideoId(),
                                LoriTuberVideoData(
                                    channel.id,
                                    UUID.randomUUID().toString(),
                                    true,
                                    gameState.worldInfo.currentTick,
                                    LoriTuberVideoContentCategory.GAMES,
                                    20,
                                    20, // 999,
                                    20, // 999,
                                    20, // 999, // 999, // gameState.random.nextInt(0, 1000),
                                    // LoriTuberVibes(gameState.random.nextLong(0, 128)),
                                    LoriTuberVibes(0),
                                    LoriTuberVibes(0), // yes this is incorrect
                                    0,
                                    0,
                                    0,
                                    videoEvents,
                                    listOf()
                                )
                            )

                            val startX = System.currentTimeMillis()


                            val vibesOfTheCurrentCategory = gameState.trendsByCategory[video.data.contentCategory]!!
                            val videoGameplayScore = (video.data.recordingScore + video.data.editingScore + video.data.thumbnailScore) / 3
                            val videoGameplayProgress = videoGameplayScore / 999.0

                            var matchedVibes = 0
                            for (vibe in LoriTuberVideoContentVibes.entries) {
                                if (vibesOfTheCurrentCategory.vibes.vibeType(vibe) == video.data.vibes.vibeType(vibe)) {
                                    matchedVibes++
                                }
                            }

                            var targetViews = 0
                            var targetLikes = 0
                            var targetDislikes = 0

                            // Process sub views
                            var subViews = 0

                            for ((category, relationship) in channel.data.channelRelationshipsV2) {
                                // For every sub you have = a view (yay)
                                if (category == video.data.contentCategory) {
                                    // All subs always view the videos BUT they will only like if we are actually vibin
                                    subViews += relationship.subscribers

                                    if (matchedVibes >= 3)
                                        targetLikes++
                                }
                            }

                            val baseViews = when (matchedVibes) {
                                6 -> 1_000_000
                                5 -> 750_000
                                4 -> 250_000
                                3 -> 100_000
                                2 -> 100_000
                                1 -> 100_000
                                0 -> 100_000
                                else -> error("Unsupported matched vibes count $matchedVibes")
                            }

                            val chancesOfLiking: Float
                            val chancesOfDisliking: Float

                            // While we could just "reverse" the like/dislike output when it is a negative match, I think that this is better because it
                            // feels more "realistic"
                            when (matchedVibes) {
                                6 -> {
                                    chancesOfLiking = 0.06f
                                    chancesOfDisliking = 0.0f
                                }

                                5 -> {
                                    chancesOfLiking = 0.05f
                                    chancesOfDisliking = 0.0004f
                                }

                                4 -> {
                                    chancesOfLiking = 0.04f
                                    chancesOfDisliking = 0.0008f
                                }

                                3 -> {
                                    chancesOfLiking = 0.03f
                                    chancesOfDisliking = 0.0012f
                                }

                                2 -> {
                                    chancesOfLiking = 0.02f
                                    chancesOfDisliking = 0.025f
                                }

                                1 -> {
                                    chancesOfLiking = 0.01f
                                    chancesOfDisliking = 0.035f
                                }

                                0 -> {
                                    chancesOfLiking = 0.00f
                                    chancesOfDisliking = 0.06f
                                }

                                else -> error("Unsupported matched vibes count $matchedVibes")
                            }

                            // TODO: Change the views to match the algorithm boost
                            // The algorithm boost is based on the current category vibes
                            targetViews += subViews + (baseViews * easeInQuad(videoGameplayProgress)).toInt()

                            repeat(targetViews) {
                                val shouldILike = gameState.random.nextFloat()

                                // The likes and dislikes are actually NOT just cosmetic! They can be used to figure out if the vibes are good or not
                                if (chancesOfLiking > shouldILike)
                                    targetLikes++
                                else if ((chancesOfLiking + chancesOfDisliking) > shouldILike)
                                    targetDislikes++
                            }

                            // If the vibes match, then that's awesomesauce!!!
                            // Bad content does not hurt your relationship, but it also doesn't increase it, so you do get punished by the daily decay
                            if (matchedVibes >= 3) {
                                // TODO: Give a different relationship reward depending on how many vibes we matched
                                //   ^ Do we really need this?

                                // Give a positive feedback to the category because we made a video that we viiibe
                                channel.addRelationshipOfCategory(
                                    video.data.contentCategory,
                                    // A short content takes ~1 day to make
                                    // A medium content takes ~3 days to make
                                    // A long content takes ~7 days to make
                                    // Then you consider that every day you lose -2 relationship
                                    //
                                    // Previously we let users create different video lengths
                                    // But honestly that is a bit pointless, so now there's only a single video length
                                    12
                                )
                            }

                            // TODO: Refactor this
                            fun easeOutQuint(x: Double): Double {
                                return 1 - Math.pow(1 - x, 5.0)
                            }

                            // Attempt to calculate each video event
                            var lastViews = 0
                            var lastLikes = 0
                            var lastDislikes = 0

                            val ticksUntilEndOfEngagement = when {
                                100 > targetViews -> ONE_DAY_IN_TICKS
                                1_000 > targetViews -> ONE_DAY_IN_TICKS * 3
                                10_000 > targetViews -> ONE_DAY_IN_TICKS * 5
                                else -> ONE_DAY_IN_TICKS * 7
                            }

                            repeat(ticksUntilEndOfEngagement) { // 7 days
                                val progress = it / ticksUntilEndOfEngagement.toDouble()
                                val currentViews = (targetViews * easeOutQuint(progress)).toInt()
                                val currentLikes = (targetLikes * easeOutQuint(progress)).toInt()
                                val currentDislikes = (targetDislikes * easeOutQuint(progress)).toInt()

                                val diffViews = currentViews - lastViews
                                val diffLikes = currentLikes - lastLikes
                                val diffDislikes = currentDislikes- lastDislikes

                                if (diffViews != 0 || diffLikes != 0 || diffDislikes != 0) {
                                    videoEvents[it.toLong()] = listOf(
                                        LoriTuberVideoEvent.AddEngagement(
                                            diffViews,
                                            diffLikes,
                                            diffDislikes
                                        )
                                    )
                                }

                                lastViews = currentViews
                                lastLikes = currentLikes
                                lastDislikes = currentDislikes
                            }

                            println("Took ${(System.currentTimeMillis() - startX).milliseconds} to process the video")

                            channel.isDirty = true
                            video.isDirty = true

                            gameState.videosById[video.id] = video

                            println("Result: ${video}")
                            println("Matched vibes: $matchedVibes")
                            println("Base Views: $baseViews")
                            println("Target Views: $targetViews")
                            println("Target Likes: $targetLikes")
                            println("Target Dislikes: $targetDislikes")
                        }

                        /* if (line == "spawn_video") {
                            val channel = gameState.channels.first()

                            val videoEvents = mutableMapOf<Long, List<LoriTuberVideoEvent>>()

                            val video = LoriTuberVideo(
                                gameState.nextVideoId(),
                                LoriTuberVideoData(
                                    channel.id,
                                    true,
                                    gameState.worldInfo.currentTick,
                                    LoriTuberVideoContentCategory.GAMES,
                                    LoriTuberContentLength.MEDIUM,
                                    20, // 999,
                                    20, // 999,
                                    20, // 999, // 999, // gameState.random.nextInt(0, 1000),
                                    // LoriTuberVibes(gameState.random.nextLong(0, 128)),
                                    LoriTuberVibes(0),
                                    0,
                                    0,
                                    0,
                                    videoEvents
                                )
                            )

                            // In Lorituber, to mimick videos and engagement, we'll do something like this:
                            // If a super viewer likes a video, that means that the super viewer relationship with the channel increased
                            // If a super viewer dislikes a video, that means that the super viewer relationship with the channel decreased
                            //
                            // TODO: What causes a super viewer to like a video?
                            // TODO: What causes a super viewer to dislike a video?
                            // TODO: When users will unsub from the channel? We need to figure that out later

                            // TODO: Calculate each video engagement
                            // TODO: How the score should influence the quality of the content?
                            //   Maybe thumbnail score = Increases the chances of someone watching the video
                            //   Average Recording + Editing score = Increases the chance of someone liking the video (increasing rel score)
                            val startX = System.currentTimeMillis()

                            var targetViews = 0
                            var targetLikes = 0
                            var targetDislikes = 0

                            /* for ((superViewerId, channelRelationship) in channel.data.channelRelationships.entries) {
                                val superViewer = gameState.superViewers.first { it.id == superViewerId }

                                var matchedVibes = 0
                                for (vibe in LoriTuberVideoContentVibes.entries) {
                                    if (superViewer.data.preferredVibes.vibeType(vibe) == video.data.vibes.vibeType(vibe)) {
                                        matchedVibes++
                                    }
                                }

                                repeat(channelRelationship.subscribers) {
                                    // Is this a category that I'm familiar with?
                                    if (video.data.contentCategory in superViewer.data.preferredVideoContentCategories) {
                                        // Compared to the "random viewer" checks, we actually do want to run this code within the repeat
                                        // Because this is the sub themselves WATCHING the video because they received a notification
                                        if (3 >= matchedVibes) {
                                            // Ouch... This does not vibe with us at all!
                                            targetDislikes++
                                            channel.addRelationshipOfSuperViewer(superViewer, -6)
                                            return@repeat
                                        }

                                        targetViews++

                                        val myFancyScore = gameState.random.nextInt(0, 1000)
                                        val didILikeTheRecording = video.data.recordingScore >= myFancyScore
                                        val didILikeTheEditing = video.data.editingScore >= myFancyScore

                                        if (didILikeTheRecording || didILikeTheEditing) {
                                            // TODO: We need to check if the recording AND the editing is good, then the relationship increases more
                                            targetLikes++

                                            val newRelationshipScore = when (matchedVibes) {
                                                4 -> 2
                                                5 -> 4
                                                6 -> 6
                                                7 -> 10
                                                else -> error("Unsupported vibe score count $matchedVibes")
                                            }

                                            channel.addRelationshipOfSuperViewer(superViewer, newRelationshipScore)
                                        } else {
                                            // Subs are way more cut-throat than random viewers, they deserve BETTER
                                            if (myFancyScore in 0 until 250) {
                                                targetDislikes++
                                                channel.addRelationshipOfSuperViewer(superViewer, -4)
                                            }
                                        }
                                    } else {
                                        // Ouch... This ain't a category that I like!
                                        targetDislikes++

                                        channel.addRelationshipOfSuperViewer(superViewer, -8)
                                        return@repeat
                                    }
                                }
                            } */

                            /* for (superViewer in gameState.superViewers) {
                                var superViewerLikedTheVideo = false

                                println("Using ${superViewer}")

                                // Is this a category that I'm familiar with?
                                if (video.data.contentCategory !in superViewer.data.preferredVideoContentCategories)
                                    continue

                                // Yup, it is!

                                // Now we process according to the "allocatedEngagement" of the viewer
                                //
                                // Depending on current trends, the "allocatedEngagement" may be different

                                // After testing, there is no noticeable performance difference between using "ints" and "nextInt" in a loop

                                var matchedVibes = 0
                                for (vibe in LoriTuberVideoContentVibes.entries) {
                                    if (superViewer.data.preferredVibes.vibeType(vibe) == video.data.vibes.vibeType(vibe)) {
                                        matchedVibes++
                                    }
                                }

                                // To makes things easier, we don't really need let people that wouldn't *vibe* the video to *see* the video
                                // This somewhat simulates how YouTube attempts to only give you videos that you would like to see
                                if (3 >= matchedVibes)
                                    continue

                                // The scaled thumbnail progress helps us actually setup that low thumbnail scores will have less views
                                val thumbnailScoreProgress = video.data.thumbnailScore / 999.0
                                val thumbnailScoreScaled = (video.data.thumbnailScore * thumbnailScoreProgress)

                                repeat(superViewer.data.allocatedEngagement) {
                                    val shouldISeeThisVideoBasedOnThumbnail = gameState.random.nextInt(0, 1000)

                                    // This "simulates" the YouTube feed algorithm, by only "showing" the video to people that do vibe with this video
                                    // Better vibe match = More likely are the chances for the video to be served!
                                    val shouldISeeThisVideoBasedOnVibes = gameState.random.nextInt(0, 1000)

                                    val v = when (matchedVibes) {
                                        4 -> 500
                                        5 -> 750
                                        6 -> 900
                                        7 -> 1_000
                                        else -> error("Unsupported vibe score count $matchedVibes")
                                    }

                                    if (!(shouldISeeThisVideoBasedOnVibes in 0 until v && thumbnailScoreScaled >= shouldISeeThisVideoBasedOnThumbnail))
                                        return@repeat

                                    val newViews = when (video.data.contentLength) {
                                        LoriTuberContentLength.SHORT -> {
                                            // targetViews += 10
                                            7
                                        }
                                        LoriTuberContentLength.MEDIUM -> {
                                            // targetViews += 10
                                            3
                                        }
                                        LoriTuberContentLength.LONG -> {
                                            // targetViews += 10
                                            1
                                        }
                                    }

                                    targetViews += newViews

                                    repeat(newViews) {
                                        val myFancyScore = gameState.random.nextInt(0, 1000)

                                        val average = (video.data.recordingScore + video.data.editingScore) / 2
                                        val didILikeTheVideo = average >= myFancyScore

                                        if (didILikeTheVideo) {
                                            // Yay, I liked it!
                                            targetLikes++
                                            superViewerLikedTheVideo = true
                                        }

                                        // Random viewers do not give dislikes, to make it easier for the player to know when there's legitmate viewers disliking the videos
                                    }

                                    // We don't increase the relationship for each view because that would be very OP
                                    if (superViewerLikedTheVideo) {
                                        /* channel.addRelationshipOfSuperViewer(
                                            superViewer,
                                            // A short content takes ~1 day to make
                                            // A medium content takes ~3 days to make
                                            // A long content takes ~7 days to make
                                            // Then you consider that every day you lose -2 relationship
                                            when (video.data.contentLength) {
                                                LoriTuberContentLength.SHORT -> {
                                                    1
                                                }
                                                LoriTuberContentLength.MEDIUM -> {
                                                    3
                                                }
                                                LoriTuberContentLength.LONG -> {
                                                    7
                                                }
                                            }
                                        ) */
                                    }
                                    /* if (video.data.thumbnailScore >= shouldISeeThisVideoBasedOnThumbnail && v >= shouldISeeThisVideoBasedOnVibes && contentLengthScore == 0) {
                                        // Okay, so I'm watching this video!
                                        // video.data.views++
                                        targetViews++

                                        // Should I like it?
                                        // TODO: How to figure out if we should like or dislike something?
                                        // Maybe what we should do is that, depending on the thumbnail quality, and the vibes, the recording + editing score should be used
                                        val myFancyScore = gameState.random.nextInt(0, 1000)
                                        val didILikeTheRecording = video.data.recordingScore >= myFancyScore
                                        val didILikeTheEditing = video.data.editingScore >= myFancyScore

                                        if (false) {
                                            if (didILikeTheRecording || didILikeTheEditing) {
                                                // TODO: We need to check if the recording AND the editing is good, then the relationship increases more
                                                // video.data.likes++
                                                targetLikes++

                                                val newRelationshipScore = when (matchedVibes) {
                                                    4 -> 2
                                                    5 -> 4
                                                    6 -> 6
                                                    7 -> 10
                                                    else -> error("Unsupported vibe score count $matchedVibes")
                                                }

                                                // Add relationship points if the user liked the video
                                                channel.addRelationshipOfSuperViewer(
                                                    superViewer,
                                                    newRelationshipScore
                                                )
                                            }
                                        }

                                        /* if (matchedVibes >= 6) {
                                        video.data.likes++

                                        // TODO: Relationship scores should be handled in a different way
                                        //   Maybe don't store it with the SuperViewer? I don't know if it is a good idea
                                        val data = channel.data.channelRelationships.getOrPut(superViewer.id) {
                                            LoriTuberSuperViewerChannelRelationshipData(0)
                                        }

                                        data.relationshipScore += 4
                                    }

                                    if (1 >= matchedVibes) {
                                        video.data.dislikes++
                                    } */

                                        // TODO: We should take "vibes" in consideration here
                                        /* val qualityAverage = listOf(video.data.recordingScore, video.data.editingScore).average().toInt()

                                    val shouldLike = gameState.random.nextInt(0, 1000)
                                    val shouldDislike = gameState.random.nextInt(0, 1000) */

                                        /* if (shouldLike) {
                                        video.data.likes++
                                    } */
                                        // TODO: Calculate new subs
                                        //   Subs are calculated based on the current relationship of this super viewer with the channel
                                        //   Bigger relationship = More subs generated
                                    } */
                                }
                            } */

                            // TODO: Refactor this
                            fun easeOutQuint(x: Double): Double {
                                return 1 - Math.pow(1 - x, 5.0)
                            }

                            // Attempt to calculate each video event
                            var lastViews = 0
                            var lastLikes = 0
                            var lastDislikes = 0

                            val ticksUntilEndOfEngagement = when {
                                100 > targetViews -> ONE_DAY_IN_TICKS
                                1_000 > targetViews -> ONE_DAY_IN_TICKS * 3
                                10_000 > targetViews -> ONE_DAY_IN_TICKS * 5
                                else -> ONE_DAY_IN_TICKS * 7
                            }

                            repeat(ticksUntilEndOfEngagement) { // 7 days
                                val progress = it / ticksUntilEndOfEngagement.toDouble()
                                val currentViews = (targetViews * easeOutQuint(progress)).toInt()
                                val currentLikes = (targetLikes * easeOutQuint(progress)).toInt()
                                val currentDislikes = (targetDislikes * easeOutQuint(progress)).toInt()

                                val diffViews = currentViews - lastViews
                                val diffLikes = currentLikes - lastLikes
                                val diffDislikes = currentDislikes- lastDislikes

                                if (diffViews != 0 || diffLikes != 0 || diffDislikes != 0) {
                                    videoEvents[it.toLong()] = listOf(
                                        LoriTuberVideoEvent.AddEngagement(
                                            diffViews,
                                            diffLikes,
                                            diffDislikes
                                        )
                                    )
                                }

                                lastViews = currentViews
                                lastLikes = currentLikes
                                lastDislikes = currentDislikes
                            }

                            println("Took ${(System.currentTimeMillis() - startX).milliseconds} to process the video")

                            channel.isDirty = true
                            video.isDirty = true

                            gameState.videosById[video.id] = video

                            println("Result: ${video}")
                            println("Target Views: $targetViews")
                            println("Target Likes: $targetLikes")
                            println("Target Dislikes: $targetDislikes")
                        }

                        if (line.startsWith("show_video")) {
                            val video = gameState.videos.first { it.id == line.split(" ").last().toLong() }
                            println(video)
                        }

                        if (line == "spawn_videos_10k") {
                            val channel = gameState.channels.first()

                            repeat(10_000) {
                                gameState.videos.add(
                                    LoriTuberVideo(
                                        gameState.nextVideoId(),
                                        LoriTuberVideoData(
                                            channel.id,
                                            true,
                                            gameState.worldInfo.currentTick,
                                            LoriTuberVideoContentCategory.GAMES,
                                            LoriTuberContentLength.MEDIUM,
                                            10,
                                            10,
                                            10,
                                            LoriTuberVibes(0),
                                            0,
                                            0,
                                            0,
                                            mapOf()
                                        )
                                    ).also {
                                        it.isDirty = true
                                    }
                                )
                            }
                        }

                        if (line == "spawn_videos_100k") {
                            val channel = gameState.channels.first()

                            repeat(100_000) {
                                gameState.videos.add(
                                    LoriTuberVideo(
                                        gameState.nextVideoId(),
                                        LoriTuberVideoData(
                                            channel.id,
                                            true,
                                            gameState.worldInfo.currentTick,
                                            LoriTuberVideoContentCategory.GAMES,
                                            LoriTuberContentLength.MEDIUM,
                                            10,
                                            10,
                                            10,
                                            LoriTuberVibes(0),
                                            0,
                                            0,
                                            0,
                                            mapOf()
                                        )
                                    ).also {
                                        it.isDirty = true
                                    }
                                )
                            }
                        }

                        if (line == "spawn_videos_1M") {
                            val channel = gameState.channels.first()

                            repeat(1_000_000) {
                                gameState.videos.add(
                                    LoriTuberVideo(
                                        gameState.nextVideoId(),
                                        LoriTuberVideoData(
                                            channel.id,
                                            true,
                                            gameState.worldInfo.currentTick,
                                            LoriTuberVideoContentCategory.GAMES,
                                            LoriTuberContentLength.MEDIUM,
                                            10,
                                            10,
                                            10,
                                            LoriTuberVibes(0),
                                            0,
                                            0,
                                            0,
                                            mapOf()
                                        )
                                    ).also {
                                        it.isDirty = true
                                    }
                                )
                            }
                        } */
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to execute command!" }
                    }
                }
            }
        }
    }

    fun <T> transaction(statement: Transaction.() -> T): T {
        return org.jetbrains.exposed.sql.transactions.transaction(lorituberDatabase, statement)
    }

    private fun gameLoop() {
        while (true) {
            runBlocking {
                try {
                    // We need to lock the ticking mutex when ticking the game state to avoid any concurrency issues
                    val tickStart = System.currentTimeMillis()
                    val finishedUpdateAt = tickingMutex.withLock {
                        logger.info { "Took ${System.currentTimeMillis() - tickStart}ms to get tickingMutex lock" }
                        gameState.worldInfo.currentTick++

                        val lastUpdate = gameState.worldInfo.lastUpdate
                        val currentTick = gameState.worldInfo.currentTick
                        val worldTime = WorldTime(currentTick)

                        logger.info { "Current Tick: $currentTick - Last Update Time: $lastUpdate" }

                        // Process Grocery Stock
                        if (gameState.nelsonGroceryStore.items.isEmpty() || (worldTime.hours == 8 && worldTime.minutes == 0)) {
                            gameState.nelsonGroceryStore.items.clear()

                            // Insert random quantity of items in the stock
                            for (item in NELSON_GROCERY_STORE_ITEMS) {
                                gameState.nelsonGroceryStore.items.add(
                                    LoriTuberGroceryItem(
                                        LoriTuberGroceryItemData(
                                            item.id,
                                            gameState.random.nextInt(5, 15)
                                        )
                                    )
                                )
                            }

                            gameState.nelsonGroceryStore.isDirty = true
                        }

                        logger.info { "Characters: ${gameState.characters.size}" }
                        logger.info { "Channels: ${gameState.channels.size}" }
                        logger.info { "Videos: ${gameState.videos.size}" }

                        tickTrends(currentTick)
                        tickVideos(currentTick)
                        tickCharacters(currentTick)
                        tickLots(currentTick)

                        // This feels a bit wonky but that's what we need to do
                        val finishedUpdateAt = lastUpdate + TICK_DELAY
                        gameState.worldInfo.lastUpdate = finishedUpdateAt

                        if (!isFirstTick && !cantKeepUp && gameState.worldInfo.currentTick % 20 == 0L) {
                            gameState.persist()
                        }

                        isFirstTick = false

                        return@withLock finishedUpdateAt
                    }

                    val tickEnd = System.currentTimeMillis()

                    val diff = tickEnd - tickStart

                    if (averageTickDurations.size == ((TICK_DELAY * TICKS_PER_SECOND) * 60)) {
                        averageTickDurations.removeAt(0)
                    }

                    averageTickDurations.add(diff.milliseconds)

                    // This allows the game to "catch up"
                    // This is very confusing to understand and I haven't fully understood this yet
                    // But technically we use the "finishedUpdateAt" because the finishedUpdateAt doesn't *actually* reflect the time it was processed
                    // It reflects the last time + 1000ms
                    // What we need to know is how many ticks we are *behind*
                    // Stuff to think about...
                    val timeToWait = TICK_DELAY - (tickEnd - finishedUpdateAt)

                    logger.info { "Took ${diff}ms to process Lorituber game loop" }

                    if (timeToWait > 0) {
                        logger.info { "Waiting ${timeToWait}ms before next game tick" }
                        cantKeepUp = false
                        Thread.sleep(timeToWait)
                    } else {
                        val timeBehind = timeToWait * -1
                        val ticksBehind = timeBehind / TICK_DELAY

                        cantKeepUp = true
                        logger.warn { "Can't keep up! Is the server overloaded? Running ${timeToWait * -1}ms or ${ticksBehind} ticks behind" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Something went wrong while ticking LoriTuber game loop!" }
                    // We don't know how much time has been elapsed, so we'll just wait the default time
                    Thread.sleep(TICK_DELAY.toLong())
                }
            }
        }
    }

    private fun tickCharacters(currentTick: Long) {
        logger.info { "Ticking characters..." }

        // TODO: Add character locking
        val tickableCharacters = gameState.characters

        for (character in tickableCharacters) {
            logger.info { character.data.firstName }
            val currentLot = gameState.lotsById[character.data.currentLotId]!!

            // TODO: Remove this later!!!
            // Give test items
            if (false) {
                if (!character.data.items.any { it.id == LoriTuberItems.CHEAP_TOILET.id }) {
                    character.data.items.add(
                        LoriTuberItemStackData(
                            UUID.randomUUID(),
                            LoriTuberItems.CHEAP_TOILET.id,
                            1,
                            LoriTuberItemBehaviorAttributes.Toilet(0, false, 0)
                        )
                    )
                }

                if (!character.data.items.any { it.id == LoriTuberItems.CHEAP_BED.id }) {
                    character.data.items.add(
                        LoriTuberItemStackData(
                            UUID.randomUUID(),
                            LoriTuberItems.CHEAP_BED.id,
                            1,
                            null
                        )
                    )
                }

                if (!character.data.items.any { it.id == LoriTuberItems.CHEAP_SHOWER.id }) {
                    character.data.items.add(
                        LoriTuberItemStackData(
                            UUID.randomUUID(),
                            LoriTuberItems.CHEAP_SHOWER.id,
                            1,
                            LoriTuberItemBehaviorAttributes.Shower
                        )
                    )
                }

                if (!character.data.items.any { it.id == LoriTuberItems.CHEAP_FRIDGE.id }) {
                    character.data.items.add(
                        LoriTuberItemStackData(
                            UUID.randomUUID(),
                            LoriTuberItems.CHEAP_FRIDGE.id,
                            1,
                            null
                        )
                    )
                }
            }

            when (val task = character.data.currentTask) {
                is LoriTuberTask.Sleeping -> {
                    // 8 hours
                    character.motives.addEnergyPerTicks(100.0, 480)

                    if (character.motives.energy >= 100.0)
                        character.setTask(null)
                }
                is LoriTuberTask.TakingAShower -> {
                    character.motives.addHygienePerTicks(100.0, 20)

                    if (character.motives.hygiene >= 100.0)
                        character.setTask(null)
                }
                is LoriTuberTask.UsingToilet -> {
                    character.motives.addBladderPerTicks(100.0, 15)

                    if (character.motives.bladder >= 100.0)
                        character.setTask(null)
                }
                is LoriTuberTask.UsingItem -> {
                    // TODO: Remove this maybe?
                    /* val itemThatIsBeingUsed = character.data.items.firstOrNull { it.localId == task.itemLocalId }
                    if (itemThatIsBeingUsed == null) {
                        logger.warn { "Item that was being used magically disappeared from our inventory! Resetting task..." }
                        character.setTask(null)
                    } else {
                        val bhav = LoriTuberItemBehaviors.itemToBehaviors[itemThatIsBeingUsed.id]

                        if (bhav != null) {
                            logger.info { "Ticking active item item ${itemThatIsBeingUsed} with $bhav, being used by ${character.id}" }

                            // Tick item bhav
                            bhav.activeCharacterTaskTick(gameState, currentTick, character, itemThatIsBeingUsed)
                        } else {
                            logger.warn { "Character ${character.id} is using a item (${itemThatIsBeingUsed} that doesn't have any behavior bound to it!" }
                        }
                    } */
                }
                is LoriTuberTask.Eating -> {
                    val item = LoriTuberItems.getById(task.itemId)
                    val foodAttributes = item.foodAttributes!!
                    if ((currentTick - task.startedEatingAtTick) > foodAttributes.ticks) {
                        // Finished eating the item, remove the task!
                        println("Finished food!")
                        character.setTask(null)
                    } else {
                        // We are still eating, nom om om
                        character.motives.addHunger(foodAttributes.hunger.toDouble())
                        character.motives.addBladderPerTicks(-100.0, 120)
                    }
                }
                is LoriTuberTask.PreparingFood -> {
                    val recipe = task.recipeId?.let { LoriTuberRecipes.getById(it) }
                    val targetItem = recipe?.targetItemId?.let { LoriTuberItems.getById(it) } ?: LoriTuberItems.SLOP

                    val ticks = recipe?.ticks ?: 20 // Slop

                    if ((currentTick - task.startedPreparingAtTick) > ticks) {
                        // Finished eating the item, remove the task!
                        println("Finished preparing food!")

                        // Remove the items from the inventory if the user has them
                        val itemsFromTheUserInventory = character.data.items.filter { it.id in task.items }

                        val hasEnoughItems = character.inventory.containsItems(task.items)

                        if (!hasEnoughItems) {
                            // We don't have enough items! Just reset the task and bail out!
                            character.setTask(null)
                        } else {
                            for (item in itemsFromTheUserInventory) {
                                character.inventory.removeSingleItem(item.id)
                            }

                            // Add the new item to their inventory
                            character.inventory.addItem(targetItem, 1)

                            // Update the task to null
                            character.setTask(null)
                        }
                    } else {
                        // We are still preparing, do nothing
                    }
                }
                is LoriTuberTask.WorkingOnVideo -> {
                    if (character.motives.isMoodAboveRequiredForWork()) {
                        val channel = gameState.channels.firstOrNull { it.id == task.channelId }
                        if (channel != null) {
                            val pendingVideo = channel.data.pendingVideos.firstOrNull { it.id == task.pendingVideoId }
                            if (pendingVideo != null) {
                                when (task.stage) {
                                    LoriTuberVideoStage.RECORDING -> {
                                        val inProgress = pendingVideo.recordingStage as? LoriTuberPendingVideoStageData.InProgress
                                        if (inProgress == null) {
                                            // Invalid stage, reset!
                                            character.setTask(null)
                                        } else {
                                            // How this works?
                                            // We +1 for each tick
                                            if (inProgress.progressTicks == 5L) {
                                                // Okay, so this stage is finished! Cancel the current task
                                                character.setTask(null)

                                                // Set the new recording score...
                                                pendingVideo.recordingStage = LoriTuberPendingVideoStageData.Finished(
                                                    gameState.random.nextInt(
                                                        10,
                                                        21
                                                    )
                                                )

                                                // And unlock two new stages!
                                                pendingVideo.editingStage = LoriTuberPendingVideoStageData.InProgress(0)
                                                pendingVideo.thumbnailStage = LoriTuberPendingVideoStageData.InProgress(0)
                                            } else {
                                                inProgress.progressTicks++
                                            }
                                            channel.isDirty = true
                                        }
                                    }

                                    LoriTuberVideoStage.EDITING -> {
                                        val inProgress = pendingVideo.editingStage as? LoriTuberPendingVideoStageData.InProgress
                                        if (inProgress == null) {
                                            // Invalid stage, reset!
                                            character.setTask(null)
                                        } else {
                                            // How this works?
                                            // We +1 for each tick
                                            if (inProgress.progressTicks == 5L) {
                                                // Okay, so this stage is finished! Cancel the current task
                                                character.setTask(null)

                                                // Set the new recording score...
                                                pendingVideo.editingStage = LoriTuberPendingVideoStageData.Finished(
                                                    gameState.random.nextInt(
                                                        10,
                                                        21
                                                    )
                                                )

                                                // And unlock the next stage!
                                                pendingVideo.renderingStage = LoriTuberPendingVideoStageData.InProgress(0)
                                            } else {
                                                inProgress.progressTicks++
                                            }
                                            channel.isDirty = true
                                        }
                                    }

                                    LoriTuberVideoStage.RENDERING -> {
                                        val inProgress = pendingVideo.renderingStage as? LoriTuberPendingVideoStageData.InProgress
                                        if (inProgress == null) {
                                            // Invalid stage, reset!
                                            character.setTask(null)
                                        } else {
                                            // How this works?
                                            // We +1 for each tick
                                            if (inProgress.progressTicks == 5L) {
                                                // Okay, so this stage is finished! Cancel the current task
                                                character.setTask(null)

                                                // Set the new recording score...
                                                pendingVideo.renderingStage = LoriTuberPendingVideoStageData.Finished(
                                                    gameState.random.nextInt(
                                                        10,
                                                        21
                                                    )
                                                )

                                                // And now we don't need to unlock anything rn
                                            } else {
                                                inProgress.progressTicks++
                                            }
                                            channel.isDirty = true
                                        }
                                    }

                                    LoriTuberVideoStage.THUMBNAIL -> {
                                        val inProgress =
                                            pendingVideo.thumbnailStage as? LoriTuberPendingVideoStageData.InProgress
                                        if (inProgress == null) {
                                            // Invalid stage, reset!
                                            character.setTask(null)
                                        } else {
                                            // How this works?
                                            // We +1 for each tick
                                            if (inProgress.progressTicks == 5L) {
                                                // Okay, so this stage is finished! Cancel the current task
                                                character.setTask(null)

                                                // Set the new recording score...
                                                pendingVideo.thumbnailStage = LoriTuberPendingVideoStageData.Finished(
                                                    gameState.random.nextInt(
                                                        10,
                                                        21
                                                    )
                                                )

                                                // And now we don't need to unlock anything rn
                                            } else {
                                                inProgress.progressTicks++
                                            }
                                            channel.isDirty = true
                                        }
                                    }
                                }
                            } else {
                                // Unknown pending video, reset the task!
                                character.setTask(null)
                            }
                        } else {
                            // Unknown channel, reset the task!
                            character.setTask(null)
                        }
                    } else {
                        // I'm too depressed, reset the task!
                        character.setTask(null)
                    }
                }
                null -> {} // Mó paz
            }

            val pendingPhoneCallData = character.data.pendingPhoneCall

            if (pendingPhoneCallData != null) {
                if (currentTick > pendingPhoneCallData.expiresAt) {
                    character.setPendingPhoneCall(null)
                }
            } else {
                if (character.data.ticksLived % 2 == 0L) {
                    // Ring their phone!
                    val worldTime = WorldTime(currentTick)

                    val call = if (true || worldTime.hours in 8..20) {
                        val isPrankCall = gameState.random.nextBoolean()
                        if (isPrankCall) {
                            gameState.oddCalls.random()
                        } else {
                            gameState.sonhosRewardCalls.random()
                        }
                    } else {
                        gameState.oddCalls.random()
                    }

                    character.setPendingPhoneCall(PendingPhoneCallData(currentTick + 60, call))
                }
            }

            // var isSleeping = character.data.currentTask is LoriTuberTask.Sleeping

            // TODO: Implement character free will, automatically do actions automatically to fulfill their needs
            //  See this https://youtu.be/c91IWh4agzU?t=1541
            //  Don't forget to make the characters a bit irrational! The Free Will shouldn't just be a "fill the needs myself"
            // TODO: Do we *really* need free will tho? I actually don't think that we need... ^

            // Every 5s we are going to decrease their motives
            // TODO: Better motive handling, not all motives should decrease every 5 ticks
            //  Example: Hunger should decrease FASTER than sleep
            //  Also, should we store the motives in a JSON field? maybe it would be better if we end up adding new motives
            //  Also², how can we handle motives decrease? Just ALWAYS apply it no matter what? (I don't know how TS1 handles motives)
            /* if (character.data.ticksLived % (TICKS_PER_SECOND * 5) == 0L) {
                character.motives.addHunger(-1.0)
                character.motives.addEnergy(-1.0)
            } */

            // Process character bound items that have custom behavior
            // TODO: Remove this? Objects are now ticked on the lot themselves
            for (item in character.data.items) {
                val bhav = LoriTuberItemBehaviors.itemToBehaviors[item.id]

                if (bhav != null && bhav is CharacterBoundItemBehavior<*, *>) {
                    logger.info { "Ticking item ${item} with $bhav" }

                    // Tick item bhav
                    bhav.tick(
                        gameState,
                        currentLot,
                        currentTick,
                        item,
                        character
                    )
                }
            }

            // eat twice every day!
            character.motives.addHungerPerTicks(-100.0, 60 * 12)

            // take a bath daily!
            character.motives.addHygienePerTicks(-100.0, 60 * 24)

            // sleep daily!
            // we multiply by * 18 instead of * 16
            character.motives.addEnergyPerTicks(-100.0, 60 * 18)

            // use the bathroom daily!
            // (technically you'll need to use more than once a day because you also lose bladder when eating)
            character.motives.addBladderPerTicks(-100.0, 60 * 24)

            // fun!
            // (technically you'll need to use more than once a day because you also lose bladder when eating)
            character.motives.addFunPerTicks(-100.0, 60 * 8)

            // Update the character ticks lived
            character.data.ticksLived++

            // And that the character is dirty
            character.isDirty = true
        }
    }

    private fun tickTrends(currentTick: Long) {
        // Trends change every 14 days
        if ((currentTick % ONE_DAY_IN_TICKS * 14) == 0L) {
            logger.info { "Changing current trends..." }


        }

        // And then every X ticks, we change a random "viewer" to be more aligned to the current trends
    }

    private fun tickVideos(currentTick: Long) {
        logger.info { "Ticking videos..." }

        for (video in gameState.videos) {
            val relativeTick = currentTick - video.data.postedAtTicks

            val events = video.data.videoEvents[relativeTick]
                ?: continue // No events for the current relative tick found,

            for (event in events) {
                when (event) {
                    is LoriTuberVideoEvent.AddEngagement -> {
                        video.data.views += event.views
                        video.data.likes += event.likes
                        video.data.dislikes += event.dislikes

                        video.isDirty = true
                    }
                }
            }

            logger.info { "[${video.id}] Views: ${video.data.views}" }
            logger.info { "[${video.id}] Likes: ${video.data.likes}" }
            logger.info { "[${video.id}] Dislikes: ${video.data.dislikes}" }
        }

        for (channel in gameState.channels) {
            val characterOwner = gameState.characters.first { it.id == channel.data.characterId }

            logger.info { "[Character ${channel.id}] ${characterOwner.data.ticksLived % ONE_DAY_IN_TICKS} - Subscribers: ${channel.data.channelRelationshipsV2.values.sumOf { it.subscribers }}" }

            if (characterOwner.data.ticksLived % ONE_DAY_IN_TICKS == 0L) {
                // Every one day that the character has lived, decrease their relationship score with their viewers
                for (channelRelationship in channel.data.channelRelationshipsV2.values) {
                    channelRelationship.relationshipScore = (channelRelationship.relationshipScore - 2).coerceAtLeast(0)
                }

                channel.isDirty = true
            }

            for ((superViewerId, channelRelationship) in channel.data.channelRelationshipsV2) {
                if (channelRelationship.relationshipScore >= 25) {
                    // Positive relationship
                    val relativeTimeCheck = when {
                        50 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 12 // GOOD relationship
                        75 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 8 // VERY GOOD relationship
                        90 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 4 // OUTSTANDING relationship!
                        100 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS // AMAZING relationship!
                        else -> ONE_DAY_IN_TICKS
                    }

                    if (characterOwner.data.ticksLived % relativeTimeCheck == 0L) {
                        channelRelationship.subscribers += 1
                        channel.isDirty = true
                    }
                } else if (channelRelationship.subscribers != 0) {
                    // Negative relationship
                    val relativeTimeCheck = when {
                        channelRelationship.relationshipScore == 0 -> ONE_HOUR_IN_TICKS // INCREDIBLY bad relationship
                        3 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 4 // SUPER bad relationship
                        5 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 8 // VERY bad relationship
                        10 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 12 // BAD relationship
                        else -> ONE_DAY_IN_TICKS // Anything else then it is just a relationship that is just a *bit* bad
                    }

                    if (characterOwner.data.ticksLived % relativeTimeCheck == 0L) {
                        channelRelationship.subscribers = 1.coerceAtLeast(0)
                        channel.isDirty = true
                    }
                }
            }
        }

        /* for (channel in gameState.channels) {
            val characterOwner = gameState.characters.first { it.id == channel.data.characterId }

            logger.info { "[Character ${channel.id}] ${characterOwner.data.ticksLived % ONE_DAY_IN_TICKS} - Subscribers: ${channel.data.channelRelationships.values.sumOf { it.subscribers }}" }

            /* if (characterOwner.data.ticksLived % ONE_DAY_IN_TICKS == 0L) {
                // Every one day that the character has lived, decrease their relationship score with their viewers
                for (channelRelationship in channel.data.channelRelationships.values) {
                    channelRelationship.relationshipScore = (channelRelationship.relationshipScore - 2).coerceAtLeast(0)

                    // Every day attempt to trickle subscribers to the channel every day
                    // TODO: Should we keep the sub trickle here, or should we keep it when the channel posts a video?
                    //   Maybe piggyback on the video event code and store which super viewer triggered the video
                    //   TODO for the TODO: In YouTube, it seems that videos themselves does NOT track new subs, it only tracks how many people subbed from that video to now
                    //   So technically doing it like this is not incorrect...!
                    /* if (channelRelationship.relationshipScore >= 25) {
                        channelRelationship.subscribers++

                        if (channelRelationship.relationshipScore >= 50) {
                            channelRelationship.subscribers++
                        }

                        if (channelRelationship.relationshipScore >= 75) {
                            channelRelationship.subscribers++
                        }

                        if (channelRelationship.relationshipScore >= 100) {
                            channelRelationship.subscribers++
                        }
                    } else {
                        // Oof, that's bad, that means that the user has lost their relationship with this channel
                        channelRelationship.subscribers -= 2

                        if (channelRelationship.relationshipScore == 0) {
                            // Okay that's REALLY bad
                            channelRelationship.subscribers -= 2
                        }
                    }

                    channelRelationship.subscribers = channelRelationship.subscribers.coerceAtLeast(0) */
                }

                channel.isDirty = true
            }

            for ((superViewerId, channelRelationship) in channel.data.channelRelationships) {
                val superViewer = gameState.superViewersById[superViewerId]!!
                if (channelRelationship.relationshipScore > 0) {
                    // logger.info { "Super Viewer $superViewerId relationshiop: $channelRelationship" }
                }
                val subscriberCountBasedOnAllocatedEngagement = 1 // (1 * (superViewer.data.allocatedEngagement / 20))

                if (channelRelationship.relationshipScore >= 25) {
                    // Positive relationship
                    val relativeTimeCheck = when {
                        50 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 12 // GOOD relationship
                        75 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 8 // VERY GOOD relationship
                        90 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 4 // OUTSTANDING relationship!
                        100 >= channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS // AMAZING relationship!
                        else -> ONE_DAY_IN_TICKS
                    }

                    if (characterOwner.data.ticksLived % relativeTimeCheck == 0L) {
                        channelRelationship.subscribers += subscriberCountBasedOnAllocatedEngagement
                        channel.isDirty = true
                    }
                } else if (channelRelationship.subscribers != 0) {
                    // Negative relationship
                    val relativeTimeCheck = when {
                        channelRelationship.relationshipScore == 0 -> ONE_HOUR_IN_TICKS // INCREDIBLY bad relationship
                        3 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 4 // SUPER bad relationship
                        5 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 8 // VERY bad relationship
                        10 > channelRelationship.relationshipScore -> ONE_HOUR_IN_TICKS * 12 // BAD relationship
                        else -> ONE_DAY_IN_TICKS // Anything else then it is just a relationship that is just a *bit* bad
                    }

                    if (characterOwner.data.ticksLived % relativeTimeCheck == 0L) {
                        channelRelationship.subscribers = (channelRelationship.subscribers - subscriberCountBasedOnAllocatedEngagement).coerceAtLeast(0)
                        channel.isDirty = true
                    }
                }
            } */

            logger.info { "Total ChannelRels: ${channel.data.channelRelationships.size}" }
        } */
    }

    private fun tickLots(currentTick: Long) {
        logger.info { "Ticking lots..." }

        // TODO: Don't tick inactive lots
        // TODO: We need to store which characters are active + on the current lot
        val tickableLots = gameState.lots

        for (lot in tickableLots) {
            // TODO: Maybe we should store the current characters on a list, because I think this is a bit expensive
            //   Or maybe as a easy optimization: Store lot ID -> characters OUTSIDE of this loop
            val activeCharactersOnTheCurrentLot = gameState.characters.filter { it.data.currentLotId == lot.id }

            logger.info { "Active characters on lot ${lot.id}: ${activeCharactersOnTheCurrentLot.size}" }

            for (item in lot.data.items) {
                val bhav = LoriTuberItemBehaviors.itemToBehaviors[item.id]
                val characterInteractions = mutableListOf<LotBoundItemBehavior.CharacterInteractionBuilder>()

                if (bhav != null && bhav is LotBoundItemBehavior<*, *>) {
                    logger.info { "Ticking item $item with $bhav" }

                    val characterInteraction: LotBoundItemBehavior.CharacterInteractionBuilder? = null

                    // From the active characters on this lot, is anyone using this item?
                    for (character in activeCharactersOnTheCurrentLot) {
                        val currentUseItemTask = (character.data.currentTask as? LoriTuberTask.UsingItem)

                        if (currentUseItemTask != null && currentUseItemTask.itemLocalId == item.localId) {
                            // The character is using THIS item!
                            characterInteractions.add(
                                LotBoundItemBehavior.CharacterInteractionBuilder(
                                    character,
                                    currentUseItemTask
                                )
                            )
                        }
                    }

                    logger.info { "Ticking item $item with $bhav - Character Interaction: $characterInteraction" }

                    // Tick item bhav
                    bhav.tick(gameState, lot, currentTick, item, characterInteractions)
                }
            }
        }
    }
}