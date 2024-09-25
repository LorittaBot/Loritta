package net.perfectdreams.loritta.lorituber

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.*
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoResolution
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.time.Instant
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LoriTuberServer(val pudding: Pudding) {
    companion object {
        // For comparisons:
        // The Sims 1: one in game minute = one real life second
        // The Sims Online: one in game minute = five real life seconds
        const val TICKS_PER_SECOND = 1
        const val TICK_DELAY = 1_000
        private val logger = KotlinLogging.logger {}
        const val GENERAL_INFO_KEY = "general"

        private val NELSON_GROCERY_STORE_ITEMS = listOf(
            LoriTuberItems.SLICED_BREAD,
            LoriTuberItems.GESSY_CEREAL,
            LoriTuberItems.MILK,
            LoriTuberItems.STRAWBERRY_YOGURT
        )

        private const val MINIMUM_VIBE_SCORE = 30
        private const val MAXIMUM_VIBE_SCORE = 70
    }

    val averageTickDurations = mutableListOf<Duration>()

    fun start() {
        // TODO: This SHOULD NOT be in here
        runBlocking {
            pudding.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    LoriTuberCharacters,
                    LoriTuberServerInfos,
                    LoriTuberChannels,
                    LoriTuberMails,
                    LoriTuberPendingVideos
                )
            }
        }

        thread(name = "LoriTuber Game Loop") {
            gameLoop()
        }
    }

    private fun gameLoop() {
        runBlocking {
            while (true) {
                try {
                    // Keep in mind that we NEED to let go of this transaction when waiting for the next tick!
                    // In fact, we always release on EVERY tick, even if we are behind!
                    val tickStart = System.currentTimeMillis()
                    val finishedUpdateAt = pudding.transaction {
                        val serverInfo = LoriTuberServerInfos.selectAll()
                            .where { LoriTuberServerInfos.type eq GENERAL_INFO_KEY }
                            .firstOrNull()
                            ?.get(LoriTuberServerInfos.data)
                            ?.also {
                                println("ServerInfo: $it")
                            }
                            ?.let { Json.decodeFromString<ServerInfo>(it) }

                        val lastTick: Long
                        val lastUpdate: Long

                        if (serverInfo != null) {
                            // The currentTick should ALWAYS be the previous "currentTick" + 1
                            lastTick = serverInfo.currentTick + 1
                            lastUpdate = serverInfo.lastUpdate
                        } else {
                            lastTick = -1
                            lastUpdate = System.currentTimeMillis()
                        }

                        val currentTick = lastTick
                        val worldTime = WorldTime(currentTick)

                        logger.info { "Current Tick: $currentTick - Last Update Time: $lastUpdate" }

                        // Process Grocery Stock
                        if (worldTime.hours == 8 && worldTime.minutes == 0) {
                            val stockedId = LoriTuberGroceryStocks.insertAndGetId {
                                it[LoriTuberGroceryStocks.shopId] = "lorituber:nelson_grocery_store"
                                it[LoriTuberGroceryStocks.stockedAtTick] = currentTick
                                it[LoriTuberGroceryStocks.stockedAt] = Instant.now()
                            }

                            // Insert random quantity of items in the stock
                            for (item in NELSON_GROCERY_STORE_ITEMS) {
                                repeat(pudding.random.nextInt(5, 15)) {
                                    LoriTuberGroceryItems.insert {
                                        it[LoriTuberGroceryItems.storeStock] = stockedId
                                        it[LoriTuberGroceryItems.item] = item.id
                                    }
                                }
                            }
                        }

                        // Tick viewers
                        tickViewers(currentTick)

                        if (false && currentTick % 20 == 0L) {
                            // How viewer tick works?
                            // We simulate how YouTube viewers work:
                            // A user opens YouTube, they are presented with multiple recent videos that may be "cool" for them
                            // The user clicks a video that...
                            // 1. has a good thumbnail
                            // 2. has a good title
                            // 3. is about a category that they like
                            // 4. the user does not have a bad relationship with the channel
                            //
                            // Then the user watches the video (the video gets a monetized view!), and in the video itself
                            // 1. does the user vibe with the video?
                            // 2. does the user like the video quality?
                            // The 1 and 2 are both 50% of the total video score, with a positive reception if >75% and negative reception if <25%
                            // And then stuff happens depending on if the user likes it or not
                            logger.info { "Ticking LoriTuber viewers..." }

                            // Get a random viewer that is NOT watching a specific video
                            val viewersThatArentWatchingAnything = LoriTuberViewers
                                .selectAll()
                                .where {
                                    LoriTuberViewers.watchingVideo.isNull()
                                }
                            val viewers = LoriTuberViewers.selectAll()
                                .toList()

                            // TODO: How should we tick this?
                            //  How a good video should be handled?
                            val recentVideos = LoriTuberVideos.selectAll()

                            // Get random video
                            for (video in LoriTuberVideos.selectAll()) {
                                // Does the user like the vibes of the video?
                                var worstScore = Int.MAX_VALUE
                                var bestScore = 0
                                var vibers = 0

                                for (viewer in viewers) {
                                    val viewerPreferences = LoriTuberViewerVideoPreferences
                                        .selectAll()
                                        .where { LoriTuberViewerVideoPreferences.viewer eq viewer[LoriTuberViewers.id] and (LoriTuberViewerVideoPreferences.category eq video[LoriTuberVideos.contentCategory]) }
                                        .first()

                                    val vibe1Distance = (video[LoriTuberVideos.vibe1] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe1Preference]).absoluteValue
                                    val vibe2Distance = (video[LoriTuberVideos.vibe2] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe2Preference]).absoluteValue
                                    val vibe3Distance = (video[LoriTuberVideos.vibe3] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe3Preference]).absoluteValue
                                    val vibe4Distance = (video[LoriTuberVideos.vibe4] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe4Preference]).absoluteValue
                                    val vibe5Distance = (video[LoriTuberVideos.vibe5] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe5Preference]).absoluteValue
                                    val vibe6Distance = (video[LoriTuberVideos.vibe6] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe6Preference]).absoluteValue
                                    val vibe7Distance = (video[LoriTuberVideos.vibe7] - viewerPreferences[LoriTuberViewerVideoPreferences.vibe7Preference]).absoluteValue

                                    val vibe1Score = 10 - vibe1Distance
                                    val vibe2Score = 10 - vibe2Distance
                                    val vibe3Score = 10 - vibe3Distance
                                    val vibe4Score = 10 - vibe4Distance
                                    val vibe5Score = 10 - vibe5Distance
                                    val vibe6Score = 10 - vibe6Distance
                                    val vibe7Score = 10 - vibe7Distance

                                    val vibeScore =
                                        vibe1Score + vibe2Score + vibe3Score + vibe4Score + vibe5Score + vibe6Score + vibe7Score

                                    // The max vibe score is 70
                                    // With vibes, a vibe score of vibeScore>=50 is VERY good
                                    // While 20>vibeScore is VERY bad
                                    println("${video[LoriTuberVideos.id]} Vibe score for ${viewer[LoriTuberViewers.handle]}: $vibeScore")

                                    if (30 >= vibeScore) {
                                        println("${video[LoriTuberVideos.id]} I do not vibe with this video! ${viewer[LoriTuberViewers.handle]}")
                                    } else if (vibeScore >= 50) {
                                        vibers++
                                        println("${video[LoriTuberVideos.id]} I vibe with this video! ${viewer[LoriTuberViewers.handle]}")
                                    }

                                    if (worstScore > vibeScore) {
                                        worstScore = vibeScore
                                    }

                                    if (vibeScore > bestScore) {
                                        bestScore = vibeScore
                                    }
                                }

                                println("${video[LoriTuberVideos.id]} Best Score: $bestScore; Worst Score: $worstScore; Vibers: $vibers")
                            }
                        }

                        // Tick each player
                        logger.info { "Ticking characters..." }

                        // TODO: Add character lock
                        val characters = LoriTuberCharacters
                            .selectAll()
                            .toList()

                        for (character in characters) {
                            var isSleeping = false
                            val currentTaskAsJson = character[LoriTuberCharacters.currentTask]

                            if (currentTaskAsJson != null) {
                                when (val task = Json.decodeFromString<LoriTuberTask>(currentTaskAsJson)) {
                                    is LoriTuberTask.Sleeping -> isSleeping = true
                                    is LoriTuberTask.Eating -> {
                                        val item = LoriTuberItems.getById(task.itemId)
                                        val foodAttributes = item.foodAttributes!!
                                        if ((currentTick - task.startedEatingAtTick) > foodAttributes.ticks) {
                                            // Finished eating the item, remove the task!
                                            println("Finished food!")
                                            LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                it[LoriTuberCharacters.currentTask] = null
                                            }
                                        } else {
                                            // We are still eating, nom om om
                                            LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                it[LoriTuberCharacters.hungerNeed] = character[LoriTuberCharacters.hungerNeed] + foodAttributes.hunger
                                            }
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
                                            val itemsFromTheUserInventory = LoriTuberCharacterInventoryItems.selectAll()
                                                .where {
                                                    LoriTuberCharacterInventoryItems.item inList task.items and (LoriTuberCharacterInventoryItems.owner eq character[LoriTuberCharacters.id])
                                                }
                                                .toList()

                                            var hasEnoughItems = true
                                            val itemsToBeDeleted = mutableListOf<Long>()

                                            for (recipeItemId in task.items) {
                                                val matched = itemsFromTheUserInventory.firstOrNull { it[LoriTuberCharacterInventoryItems.item] == recipeItemId }
                                                if (matched == null) {
                                                    hasEnoughItems = false
                                                    break
                                                }

                                                itemsToBeDeleted.add(matched[LoriTuberCharacterInventoryItems.id].value)
                                            }

                                            if (!hasEnoughItems) {
                                                // We don't have enough items! Just reset the task and bail out!
                                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                    // Update the task to null
                                                    it[LoriTuberCharacters.currentTask] = null
                                                }
                                            } else {
                                                // Delete the items used in the recipe
                                                LoriTuberCharacterInventoryItems.deleteWhere {
                                                    LoriTuberCharacterInventoryItems.id inList itemsToBeDeleted
                                                }

                                                // Add the new item to their inventory
                                                LoriTuberCharacterInventoryItems.insert {
                                                    it[LoriTuberCharacterInventoryItems.owner] = character[LoriTuberCharacters.id]
                                                    it[LoriTuberCharacterInventoryItems.item] = targetItem.id
                                                    it[LoriTuberCharacterInventoryItems.addedAt] = Instant.now()
                                                }

                                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                    // Update the task to null
                                                    it[LoriTuberCharacters.currentTask] = null
                                                }
                                            }
                                        } else {
                                            // We are still preparing, do nothing
                                        }
                                    }
                                    is LoriTuberTask.WorkingOnVideo -> {
                                        val pendingVideoData = LoriTuberPendingVideos.selectAll()
                                            .where {
                                                LoriTuberPendingVideos.id eq task.pendingVideoId
                                            }.firstOrNull()!!

                                        val currentStage = pendingVideoData[LoriTuberPendingVideos.currentStage]

                                        when (currentStage) {
                                            LoriTuberVideoStage.RECORDING -> {
                                                // How this works?
                                                // We +1 for each tick
                                                if (pendingVideoData[LoriTuberPendingVideos.currentStageProgressTicks] == 20L) {
                                                    // Okay, so this stage is finished! Cancel the current task
                                                    LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                        it[LoriTuberCharacters.currentTask] = null
                                                    }

                                                    // And set the new script score
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.recordingScore] = Random().nextInt(5, 15)
                                                        }
                                                    }
                                                } else {
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.currentStageProgressTicks] = LoriTuberPendingVideos.currentStageProgressTicks + 1
                                                        }
                                                    }
                                                }
                                            }
                                            LoriTuberVideoStage.EDITING -> {
                                                // How this works?
                                                // We +1 for each tick
                                                if (pendingVideoData[LoriTuberPendingVideos.currentStageProgressTicks] == 20L) {
                                                    // Okay, so this stage is finished! Cancel the current task
                                                    LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                        it[LoriTuberCharacters.currentTask] = null
                                                    }

                                                    // And set the new script score
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.editingScore] = Random().nextInt(5, 15)
                                                        }
                                                    }
                                                } else {
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.currentStageProgressTicks] = LoriTuberPendingVideos.currentStageProgressTicks + 1
                                                        }
                                                    }
                                                }
                                            }
                                            LoriTuberVideoStage.THUMBNAIL -> {
                                                // How this works?
                                                // We +1 for each tick
                                                if (pendingVideoData[LoriTuberPendingVideos.currentStageProgressTicks] == 20L) {
                                                    // Okay, so this stage is finished! Cancel the current task
                                                    LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                        it[LoriTuberCharacters.currentTask] = null
                                                    }

                                                    // And set the new script score
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.thumbnailScore] = Random().nextInt(5, 15)
                                                        }
                                                    }
                                                } else {
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.currentStageProgressTicks] = LoriTuberPendingVideos.currentStageProgressTicks + 1
                                                        }
                                                    }
                                                }
                                            }
                                            LoriTuberVideoStage.RENDERING -> {
                                                // TODO: Proper render impl
                                                // How this works?
                                                // We +1 for each tick
                                                if (pendingVideoData[LoriTuberPendingVideos.currentStageProgressTicks] == 20L) {
                                                    // Okay, so this stage is finished! Cancel the current task
                                                    LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                        it[LoriTuberCharacters.currentTask] = null
                                                    }

                                                    // And set the new script score
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.videoResolution] = LoriTuberVideoResolution.RESOLUTION_360P
                                                        }
                                                    }
                                                } else {
                                                    LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                        with(SqlExpressionBuilder) {
                                                            it[LoriTuberPendingVideos.currentStageProgressTicks] = LoriTuberPendingVideos.currentStageProgressTicks + 1
                                                        }
                                                    }
                                                }
                                            }
                                            LoriTuberVideoStage.FINISHED -> error("Can't work on a video that is finished!")
                                        }

                                        /* val stageScore = when (currentStage) {
                                            LoriTuberVideoStage.SCRIPT -> pendingVideoData[LoriTuberPendingVideos.scriptScore]
                                            LoriTuberVideoStage.RECORDING -> pendingVideoData[LoriTuberPendingVideos.recordingScore]
                                            LoriTuberVideoStage.EDITING -> pendingVideoData[LoriTuberPendingVideos.editingScore]
                                            LoriTuberVideoStage.THUMBNAIL -> pendingVideoData[LoriTuberPendingVideos.thumbnailScore]
                                            LoriTuberVideoStage.RENDERING -> error("Can't work on a video that is being rendered!")
                                            LoriTuberVideoStage.FINISHED -> error("Can't work on a video that is finished!")
                                        }

                                        // How it works:
                                        // You can work on a video for whatever time you want, but you have a "max soft cap" of how many points you are able to get
                                        // While you can work for more time than the cap, you end up getting waaaaay less points (diminishing returns)
                                        // You can go back to a previous step, but that requires undoing all points + wiping each subsequent category
                                        // TODO: Properly calculate the script cap by getting the player's character skills
                                        val maxSoftScriptCap = 10

                                        // The first 75% are easy to get, the last 25% are harder
                                        val fastScriptCap = (maxSoftScriptCap * 0.75).toInt()

                                        val maxRandomCap = if (fastScriptCap > stageScore)
                                            10
                                        else if (maxSoftScriptCap > stageScore)
                                            25
                                        else (stageScore * 2).coerceAtLeast(25)

                                        println("Max random cap: $maxRandomCap")

                                        val r = Random()
                                        val v = r.nextInt(0, maxRandomCap)

                                        if (v == 0) {
                                            println("Giving point")
                                            LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                with(SqlExpressionBuilder) {
                                                    when (currentStage) {
                                                        LoriTuberVideoStage.SCRIPT -> it[LoriTuberPendingVideos.scriptScore] = LoriTuberPendingVideos.scriptScore + 1
                                                        LoriTuberVideoStage.RECORDING -> it[LoriTuberPendingVideos.recordingScore] = LoriTuberPendingVideos.recordingScore + 1
                                                        LoriTuberVideoStage.EDITING -> it[LoriTuberPendingVideos.editingScore] = LoriTuberPendingVideos.editingScore + 1
                                                        LoriTuberVideoStage.THUMBNAIL -> it[LoriTuberPendingVideos.thumbnailScore] = LoriTuberPendingVideos.thumbnailScore + 1
                                                        LoriTuberVideoStage.RENDERING -> error("Can't work on a video that is being rendered!")
                                                        LoriTuberVideoStage.FINISHED -> error("Can't work on a video that is finished!")
                                                    }
                                                }
                                            }
                                        }

                                        // TODO: Reimplement task halt for when the character is depressed
                                        LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                            with(SqlExpressionBuilder) {
                                                it[LoriTuberPendingVideos.renderingProgress] = LoriTuberPendingVideos.renderingProgress + 1.0
                                            }
                                        } */

                                        /* if (character[LoriTuberCharacters.energyNeed] == 0.0) {
                                            // If yes, we will reset the current task
                                            LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                                it[LoriTuberCharacters.currentTask] = null
                                            }
                                        } else {
                                            // Increment the video progress
                                            LoriTuberPendingVideos.update({ LoriTuberPendingVideos.id eq task.pendingVideoId }) {
                                                with(SqlExpressionBuilder) {
                                                    it[LoriTuberPendingVideos.percentage] =
                                                        LoriTuberPendingVideos.percentage + 1.0
                                                }
                                            }
                                        } */
                                    }
                                }
                            }

                            // TODO: Implement character free will, automatically do actions automatically to fulfill their needs
                            //  See this https://youtu.be/c91IWh4agzU?t=1541
                            //  Don't forget to make the characters a bit irrational! The Free Will shouldn't just be a "fill the needs myself"

                            // Every 5s we are going to decrease their motives
                            // TODO: Better motive handling, not all motives should decrease every 5 ticks
                            //  Example: Hunger should decrease FASTER than sleep
                            //  Also, should we store the motives in a JSON field? maybe it would be better if we end up adding new motives
                            //  Also², how can we handle motives decrease? Just ALWAYS apply it no matter what? (I don't know how TS1 handles motives)
                            if (currentTick % (TICKS_PER_SECOND * 5) == 0L) {
                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                    it[LoriTuberCharacters.hungerNeed] = (character[LoriTuberCharacters.hungerNeed] - 1.0).coerceAtLeast(0.0)

                                    if (isSleeping) {
                                        val newEnergy = (character[LoriTuberCharacters.energyNeed] + 1.0).coerceIn(0.0, 100.0)
                                        it[LoriTuberCharacters.energyNeed] = newEnergy

                                        if (newEnergy >= 100.0) {
                                            // Stop sleeping!
                                            it[LoriTuberCharacters.currentTask] = null
                                        }
                                    } else {
                                        it[LoriTuberCharacters.energyNeed] = (character[LoriTuberCharacters.energyNeed] - 1.0).coerceAtLeast(0.0)
                                    }
                                }
                            }
                        }

                        // Update the character ticks lived
                        // TODO: Add character locking
                        LoriTuberCharacters.update({ LoriTuberCharacters.id inList characters.map { it[LoriTuberCharacters.id] }}) {
                            with(SqlExpressionBuilder) {
                                it[LoriTuberCharacters.ticksLived] = LoriTuberCharacters.ticksLived + 1
                            }
                        }

                        // This feels a bit wonky but that's what we need to do
                        val finishedUpdateAt = lastUpdate + TICK_DELAY

                        LoriTuberServerInfos.upsert(LoriTuberServerInfos.type) {
                            it[LoriTuberServerInfos.type] = GENERAL_INFO_KEY
                            it[LoriTuberServerInfos.data] = Json.encodeToString(
                                ServerInfo(
                                    currentTick,
                                    finishedUpdateAt,
                                    if (averageTickDurations.isNotEmpty())
                                        averageTickDurations.map { it.inWholeMilliseconds }.average()
                                    else
                                        null
                                )
                            )
                        }

                        return@transaction finishedUpdateAt
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
                        Thread.sleep(timeToWait)
                    } else {
                        val timeBehind = timeToWait * -1
                        val ticksBehind = timeBehind / TICK_DELAY

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

    private fun tickViewers(currentTick: Long) {
        logger.info { "Ticking LoriTuber viewers..." }

        // First we are going to tick the video that the user is watching
        val viewersThatAreWatchingSomething = LoriTuberViewers
            .innerJoin(LoriTuberVideos)
            .selectAll()
            .where {
                LoriTuberViewers.watchingVideo.isNotNull()
            }

        // Have we watched it enough tho?
        for (viewer in viewersThatAreWatchingSomething) {
            val watchCooldownTicks = viewer[LoriTuberViewers.watchCooldownTicks] ?: error("Viewer ${viewer[LoriTuberViewers.id]} is watching a video, but watchCooldownTicks is null! Bug?")

            if (currentTick > watchCooldownTicks) {
                println("I (${viewer[LoriTuberViewers.handle]}) finished video ${viewer[LoriTuberVideos.id]}")
                // Hey, we finished the video, yay!!!
                LoriTuberViewers.update({ LoriTuberViewers.id eq viewer[LoriTuberViewers.id] }) {
                    it[LoriTuberViewers.watchingVideo] = null
                    it[LoriTuberViewers.watchCooldownTicks] = null
                }
            }
        }

        // Now let's get all viewers that aren't watching something
        val currentDayTick = currentTick % 1_440

        val startR = System.currentTimeMillis()
        val viewersThatArentWatchingSomethingAndAreActive = LoriTuberViewers
            .selectAll()
            .where {
                LoriTuberViewers.watchingVideo.isNull() and (LoriTuberViewers.watchCooldownTicks greaterEq currentTick or LoriTuberViewers.watchCooldownTicks.isNull())
            }
            .toList()
            .also {
                println("viewersThatArentWatchingSomething: ${it.size}")
            }
            .filter {
                return@filter true

                // TODO: Can't we query them directly on the SQL query? For some reason Exposed can't do it yet...
                val activityStartTicks = it[LoriTuberViewers.activityStartTicks]
                val activityEndTicks = it[LoriTuberViewers.activityEndTicks]

                if (activityEndTicks > activityStartTicks) {
                    // The end tick is larger than the activityEndTicks, that means that everything is on a single day
                    return@filter currentDayTick in activityStartTicks..activityEndTicks
                } else {
                    // The start tick is larger than the activityEndTicks, that means that it is overflowing to the next day
                    if (currentDayTick > activityStartTicks)
                        return@filter true

                    if (activityEndTicks > currentDayTick)
                        return@filter true

                    return@filter false
                }
            }

        println("viewersThatArentWatchingSomethingAndAreActive (currentDayTick: $currentDayTick): ${System.currentTimeMillis() - startR}ms ${viewersThatArentWatchingSomethingAndAreActive.size}")

        // How viewer tick works?
        // We simulate how YouTube viewers work:
        // A user opens YouTube, they are presented with multiple recent videos that may be "cool" for them
        // The user clicks a video that...
        // TODO: Update the following description
        // 1. has a good thumbnail
        // 2. has a good title
        // 3. is about a category that they like
        // 4. the user does not have a bad relationship with the channel
        //
        // Then the user watches the video (the video gets a monetized view!), and in the video itself
        // 1. does the user vibe with the video?
        // 2. does the user like the video quality?
        // The 1 and 2 are both 50% of the total video score, with a positive reception if >75% and negative reception if <25%
        // And then stuff happens depending on if the user likes it or not
        //
        // To make things more realistic, we pretend that we DO NOT know what are the vibes of the video themselves, only the vibes of the NPCs
        // The user only finds out the vibes WHEN watching the video
        //
        // TODO: How should we tick this?
        //  How a good video should be handled?
        // val recentVideos = LoriTuberVideos.selectAll()

        // We'll now create a personalized home page for each viewer
        // Optimization: Query all video preferences for all viewers BEFORE
        val start0 = System.currentTimeMillis()
        val startA = System.currentTimeMillis()

        /* fun longToBitSet(value: Long): BitSet {
            val bitSet = BitSet()
            for (i in 0 until Long.SIZE_BITS) {
                if ((value shr i) and 1L == 1L) {
                    bitSet.set(i)
                }
            }
            return bitSet
        }

        val viewersVideoPreferencesX = viewersThatArentWatchingSomethingAndAreActive.map {
            val vibes1 = it[LoriTuberViewers.vibesCategory1]?.let { longToBitSet(it) }
            val vibes2 = it[LoriTuberViewers.vibesCategory2]?.let { longToBitSet(it) }
            val vibes3 = it[LoriTuberViewers.vibesCategory3]?.let { longToBitSet(it) }
            val vibes4 = it[LoriTuberViewers.vibesCategory4]?.let { longToBitSet(it) }
            val vibes5 = it[LoriTuberViewers.vibesCategory5]?.let { longToBitSet(it) }
            val vibes6 = it[LoriTuberViewers.vibesCategory6]?.let { longToBitSet(it) }
            val vibes7 = it[LoriTuberViewers.vibesCategory7]?.let { longToBitSet(it) }
            val vibes8 = it[LoriTuberViewers.vibesCategory8]?.let { longToBitSet(it) }
            val vibes9 = it[LoriTuberViewers.vibesCategory9]?.let { longToBitSet(it) }
            val vibes10 = it[LoriTuberViewers.vibesCategory10]?.let { longToBitSet(it) }
        }
        println("just the map: ${System.currentTimeMillis() - startA}ms ${viewersVideoPreferencesX.size}") */

        // test iteration
        /* val justIter = System.currentTimeMillis()
        for (iter in viewersVideoPreferencesX) {
            continue
        }
        println("just the iteration: ${System.currentTimeMillis() - justIter}ms")
        println("iteration #3: ${System.currentTimeMillis() - start0}ms") */

        // val start1 = System.currentTimeMillis()
        /* val viewersVideoPreferences = viewersThatArentWatchingSomethingAndAreActive.chunked(65_535).flatMap { chunk ->
            LoriTuberViewerVideoPreferences.selectAll()
                .where {
                    LoriTuberViewerVideoPreferences.viewer inList chunk.map { it[LoriTuberViewers.id] }
                }
                .toList()
        }.groupBy { it[LoriTuberViewerVideoPreferences.viewer].value } */
        // println("viewersVideoPreferences: ${System.currentTimeMillis() - start1}ms (${viewersVideoPreferences.size})")

        // Optimization: Querying videos for all viewers is also incredibly slow
        // So, as an alternative, we'll query all videos
        val startX = System.currentTimeMillis()
        val recentVideos = LoriTuberVideos
            .selectAll()
            .where {
                (LoriTuberVideos.postedAtTicks greater (currentTick - 1_440))
            }
            .toList()
        println("recentVideos: ${System.currentTimeMillis() - startX}ms")

        val startZ = System.currentTimeMillis()
        val watchedVideos = LoriTuberViewerViews
            .select(LoriTuberViewerViews.owner, LoriTuberViewerViews.video)
            .where {
                LoriTuberViewerViews.video inList recentVideos.map { it[LoriTuberVideos.id] }
            }
            .toList()

        val watchedVideosByViewers = mutableMapOf<Long, MutableSet<Long>>()
        for (viewerViews in watchedVideos) {
            val videoIds = watchedVideosByViewers.getOrPut(viewerViews[LoriTuberViewerViews.owner].value) { mutableSetOf() }
            videoIds.add(viewerViews[LoriTuberViewerViews.video].value)
        }
        println("watchedVideos: ${System.currentTimeMillis() - startZ}ms")

        // Optimization: Batch insert new views/likes/dislikes
        val newViewerLikes = mutableListOf<ViewerLike>()
        val newViewerDislikes = mutableListOf<ViewerDislike>()
        val newViewerViews = mutableListOf<ViewerView>()
        val newRelationshipChanges = mutableListOf<ViewerRelationshipChange>()

        // Do not process all that noise if there isn't any recentVideos
        if (recentVideos.isNotEmpty()) {
            // TODO: How can we tick in a "trickle down" views manner?
            //  (Somewhat solved by the active time, should also have less issues with more videos in the game)
            val tickVideos = System.currentTimeMillis()
            var idx = 0
            for (viewer in viewersThatArentWatchingSomethingAndAreActive) {
                if (idx % 1_000 == 0) {
                    println("Current $idx")
                }
                idx++
                // TODO: If we are subscribed to channels, check if we have any new videos from that channel first!
                // val myVideoPreferences = viewersVideoPreferences[viewer[LoriTuberViewers.id].value]!!
                val likedCategories = mutableSetOf<LoriTuberVideoContentCategory>()
                if (viewer[LoriTuberViewers.vibesCategory1] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.ANIMATION)
                if (viewer[LoriTuberViewers.vibesCategory2] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.GAMES)
                if (viewer[LoriTuberViewers.vibesCategory3] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.COMEDY)
                if (viewer[LoriTuberViewers.vibesCategory4] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.BEAUTY)
                if (viewer[LoriTuberViewers.vibesCategory5] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.EDUCATION)
                if (viewer[LoriTuberViewers.vibesCategory6] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.TECHNOLOGY)
                if (viewer[LoriTuberViewers.vibesCategory7] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.REAL_LIFE)
                if (viewer[LoriTuberViewers.vibesCategory8] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.DOCUMENTARY)
                if (viewer[LoriTuberViewers.vibesCategory9] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.FINANCE)
                if (viewer[LoriTuberViewers.vibesCategory10] != null)
                    likedCategories.add(LoriTuberVideoContentCategory.POLITICS)

                val videosIdsThatIHaveWatched = watchedVideosByViewers[viewer[LoriTuberViewers.id].value] ?: setOf()

                // This is annoying as heck, but there isn't a clean way of doing everything on the PostgreSQL side
                val recentVideosThatWeHaventWatchedYet = recentVideos
                    .filter { it[LoriTuberVideos.id].value !in videosIdsThatIHaveWatched }
                    // Also filter only the categories we like
                    .filter { it[LoriTuberVideos.contentCategory] in likedCategories }

                if (recentVideosThatWeHaventWatchedYet.isEmpty()) {
                    // logger.warn { "I (${viewer[LoriTuberViewers.handle]}) don't have any available videos for me to watch!" }
                    continue
                }

                // Now we'll filter the videos using the vibe boost algorithm™
                val recentVideosWithVibeAlgo = mutableMapOf<ResultRow, Int>()
                val start3 = System.currentTimeMillis()
                for (video in recentVideosThatWeHaventWatchedYet) {
                    var algoVibeBoostScore = 0

                    val myPreferencesOfThisCategory = when (video[LoriTuberVideos.contentCategory]) {
                        LoriTuberVideoContentCategory.ANIMATION -> viewer[LoriTuberViewers.vibesCategory1]
                        LoriTuberVideoContentCategory.GAMES -> viewer[LoriTuberViewers.vibesCategory2]
                        LoriTuberVideoContentCategory.COMEDY -> viewer[LoriTuberViewers.vibesCategory3]
                        LoriTuberVideoContentCategory.BEAUTY -> viewer[LoriTuberViewers.vibesCategory4]
                        LoriTuberVideoContentCategory.EDUCATION -> viewer[LoriTuberViewers.vibesCategory5]
                        LoriTuberVideoContentCategory.TECHNOLOGY -> viewer[LoriTuberViewers.vibesCategory6]
                        LoriTuberVideoContentCategory.REAL_LIFE -> viewer[LoriTuberViewers.vibesCategory7]
                        LoriTuberVideoContentCategory.DOCUMENTARY -> viewer[LoriTuberViewers.vibesCategory8]
                        LoriTuberVideoContentCategory.FINANCE -> viewer[LoriTuberViewers.vibesCategory9]
                        LoriTuberVideoContentCategory.POLITICS -> viewer[LoriTuberViewers.vibesCategory10]
                    }!!

                    val vibe1 = getVibeScore(myPreferencesOfThisCategory, 0)
                    val vibe2 = getVibeScore(myPreferencesOfThisCategory, 1)
                    val vibe3 = getVibeScore(myPreferencesOfThisCategory, 2)
                    val vibe4 = getVibeScore(myPreferencesOfThisCategory, 3)
                    val vibe5 = getVibeScore(myPreferencesOfThisCategory, 4)
                    val vibe6 = getVibeScore(myPreferencesOfThisCategory, 5)
                    val vibe7 = getVibeScore(myPreferencesOfThisCategory, 6)

                    if (false) {
                        val likes = LoriTuberViewerLikes.selectAll()
                            .where {
                                LoriTuberViewerLikes.video eq video[LoriTuberVideos.id]
                            }.toList()

                        val dislikes = LoriTuberViewerDislikes.selectAll()
                            .where {
                                LoriTuberViewerDislikes.video eq video[LoriTuberVideos.id]
                            }.toList()

                        // At least FOUR users must have reacted postively/negatively to the video for us to use the vibe algo boost
                        if (likes.size + dislikes.size > 4) {
                            val vibe1Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe1] } + dislikes.map { it[LoriTuberViewerDislikes.vibe1] }).average()
                            val vibe2Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe2] } + dislikes.map { it[LoriTuberViewerDislikes.vibe2] }).average()
                            val vibe3Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe3] } + dislikes.map { it[LoriTuberViewerDislikes.vibe3] }).average()
                            val vibe4Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe4] } + dislikes.map { it[LoriTuberViewerDislikes.vibe4] }).average()
                            val vibe5Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe5] } + dislikes.map { it[LoriTuberViewerDislikes.vibe5] }).average()
                            val vibe6Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe6] } + dislikes.map { it[LoriTuberViewerDislikes.vibe6] }).average()
                            val vibe7Avg =
                                (likes.map { it[LoriTuberViewerLikes.vibe7] } + dislikes.map { it[LoriTuberViewerDislikes.vibe7] }).average()

                            fun calculatePreferenceBoostScore(myPreference: Int, averageVibe: Double): Int {
                                if (myPreference == -1) {
                                    if (averageVibe in -1.0..-0.5)
                                        return 2
                                    if (averageVibe in 0.5..1.0)
                                        return 0
                                    return 1
                                }

                                if (myPreference == 1) {
                                    if (averageVibe in 0.5..1.0)
                                        return 2
                                    if (averageVibe in -1.0..-0.5)
                                        return 0
                                    return 1
                                }

                                error("Something went terribly wrong! My preference is $myPreference and that's not supported!")
                            }

                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe1,
                                vibe1Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe2,
                                vibe2Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe3,
                                vibe3Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe4,
                                vibe4Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe5,
                                vibe5Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe6,
                                vibe6Avg
                            )
                            algoVibeBoostScore += calculatePreferenceBoostScore(
                                vibe7,
                                vibe7Avg
                            )
                        }
                    }

                    recentVideosWithVibeAlgo[video] = algoVibeBoostScore
                }
                // println("recentVideosWithVibeAlgo: ${System.currentTimeMillis() - start3}ms")

                val recentVideosSortedByVibeAlgo = recentVideosWithVibeAlgo
                    .entries
                    .sortedByDescending { it.value }
                    .take(6) // Only 6 videos for us
                    .map { it.key }

                /* val myRelationshipWithTheChannels = LoriTuberViewerChannelRelationships.selectAll()
                    .where {
                        LoriTuberViewerChannelRelationships.channel inList recentVideosThatWeHaventWatchedYet.map { it[LoriTuberVideos.channel] } and (LoriTuberViewerChannelRelationships.owner eq viewer[LoriTuberViewers.id])
                    } */
                val myRelationshipWithTheChannels = listOf<ResultRow>()

                val videoRanks = mutableMapOf<ResultRow, Int>()

                // The "recentVideosSortedByVibeAlgo" is what is being "served" to the user (example: YouTube home page)
                // Now it's the user's turn to figure out if they will click on the video or not
                for (video in recentVideosSortedByVibeAlgo) {
                    var videoPoints = 0

                    // Keep in mind that here we are "shopping" a video to watch, we haven't clicked at the video yet, so we can only go over...
                    // 1. the user relationship with the channel
                    // 2. the thumbnail score
                    // 3. the length of the content (can we watch it?)

                    // TODO: Content preference + Will the video that I want to watch go over my activity hours?
                    // val myPreferencesOfThisCategory = myVideoPreferences.first { it[LoriTuberViewerVideoPreferences.category] == video[LoriTuberVideos.contentCategory] }
                    val myRelationshipPointsWithTheChannel = myRelationshipWithTheChannels.firstOrNull { it[LoriTuberViewerChannelRelationships.channel] == video[LoriTuberVideos.channel] }
                        ?.get(LoriTuberViewerChannelRelationships.relationshipPoints) ?: 0

                    videoPoints += myRelationshipPointsWithTheChannel
                    videoPoints += video[LoriTuberVideos.thumbnailScore]

                    videoRanks[video] = videoPoints
                }

                // logger.info { "Self video ranking (${viewer[LoriTuberViewers.handle]}): ${videoRanks.entries.sortedByDescending { it.value }}" }

                val bestVideoForMeToWatch = videoRanks.entries
                    .sortedByDescending { it.value }
                    .maxBy { it.value }
                    .key

                val expiresAfterTicks = when (bestVideoForMeToWatch[LoriTuberVideos.contentLength]) {
                    LoriTuberContentLength.SHORT -> {
                        1
                    }

                    LoriTuberContentLength.MEDIUM -> {
                        15
                    }

                    LoriTuberContentLength.LONG -> {
                        60
                    }
                }

                // If selected, then it means that the user has WATCHED the video
                // So we insert a view!
                // println("I'm (${viewer[LoriTuberViewers.handle]}) now watching ${bestVideoForMeToWatch[LoriTuberVideos.id]}")

                val now = Instant.now()
                newViewerViews.add(
                    ViewerView(
                        viewer[LoriTuberViewers.id].value,
                        bestVideoForMeToWatch[LoriTuberVideos.id].value,
                        currentTick
                    )
                )

                // And we also update the user viewer cooldown
                // The % 8 is used to add a bit of offset to avoid all viewers watching/finishing all at the same time
                val watchCooldownTicks = currentTick + expiresAfterTicks + (viewer[LoriTuberViewers.id].value % 8)
                /* LoriTuberViewers.update({ LoriTuberViewers.id eq viewer[LoriTuberViewers.id] }) {
                    it[LoriTuberViewers.watchingVideo] = bestVideoForMeToWatch[LoriTuberVideos.id]
                    it[LoriTuberViewers.watchCooldownTicks] = watchCooldownTicks
                } */

                // Check vibe match score
                var vibeScore = 0
                val myPreferencesOfThisCategory = when (bestVideoForMeToWatch[LoriTuberVideos.contentCategory]) {
                    LoriTuberVideoContentCategory.ANIMATION -> viewer[LoriTuberViewers.vibesCategory1]
                    LoriTuberVideoContentCategory.GAMES -> viewer[LoriTuberViewers.vibesCategory2]
                    LoriTuberVideoContentCategory.COMEDY -> viewer[LoriTuberViewers.vibesCategory3]
                    LoriTuberVideoContentCategory.BEAUTY -> viewer[LoriTuberViewers.vibesCategory4]
                    LoriTuberVideoContentCategory.EDUCATION -> viewer[LoriTuberViewers.vibesCategory5]
                    LoriTuberVideoContentCategory.TECHNOLOGY -> viewer[LoriTuberViewers.vibesCategory6]
                    LoriTuberVideoContentCategory.REAL_LIFE -> viewer[LoriTuberViewers.vibesCategory7]
                    LoriTuberVideoContentCategory.DOCUMENTARY -> viewer[LoriTuberViewers.vibesCategory8]
                    LoriTuberVideoContentCategory.FINANCE -> viewer[LoriTuberViewers.vibesCategory9]
                    LoriTuberVideoContentCategory.POLITICS -> viewer[LoriTuberViewers.vibesCategory10]
                }!!

                val vibe1 = getVibeScore(myPreferencesOfThisCategory, 0)
                val vibe2 = getVibeScore(myPreferencesOfThisCategory, 1)
                val vibe3 = getVibeScore(myPreferencesOfThisCategory, 2)
                val vibe4 = getVibeScore(myPreferencesOfThisCategory, 3)
                val vibe5 = getVibeScore(myPreferencesOfThisCategory, 4)
                val vibe6 = getVibeScore(myPreferencesOfThisCategory, 5)
                val vibe7 = getVibeScore(myPreferencesOfThisCategory, 6)
                val myRelationshipPointsWithTheChannel = myRelationshipWithTheChannels.firstOrNull { it[LoriTuberViewerChannelRelationships.channel] == bestVideoForMeToWatch[LoriTuberVideos.channel] }
                    ?.get(LoriTuberViewerChannelRelationships.relationshipPoints) ?: 0

                if (vibe1 == bestVideoForMeToWatch[LoriTuberVideos.vibe1])
                    vibeScore++
                if (vibe2 == bestVideoForMeToWatch[LoriTuberVideos.vibe2])
                    vibeScore++
                if (vibe3 == bestVideoForMeToWatch[LoriTuberVideos.vibe3])
                    vibeScore++
                if (vibe4 == bestVideoForMeToWatch[LoriTuberVideos.vibe4])
                    vibeScore++
                if (vibe5 == bestVideoForMeToWatch[LoriTuberVideos.vibe5])
                    vibeScore++
                if (vibe6 == bestVideoForMeToWatch[LoriTuberVideos.vibe6])
                    vibeScore++
                if (vibe7 == bestVideoForMeToWatch[LoriTuberVideos.vibe7])
                    vibeScore++

                // The max vibe score is 7
                // With vibes, a vibe score of vibeScore>=5 is VERY good
                // While 2>=vibeScore is VERY bad
                // Anything else is considered "neutral"
                // println("${bestVideoForMeToWatch[LoriTuberVideos.id]} Vibe score for ${viewer[LoriTuberViewers.handle]}: $vibeScore")

                // TODO: The video quality (recording, editing, video stream quality) should also affect the relative relationship points
                val relativeRelationshipPoints = when (vibeScore) {
                    0 -> -6
                    1 -> -2
                    2 -> 0
                    3 -> 0
                    4 -> 0
                    5 -> 0
                    6 -> 2
                    7 -> 6
                    else -> error("Invalid vibe score! $vibeScore")
                }

                // println("${viewer[LoriTuberViewers.handle]} is changing to $relativeRelationshipPoints")

                newRelationshipChanges.add(
                    ViewerRelationshipChange(
                        viewer[LoriTuberViewers.id].value,
                        bestVideoForMeToWatch[LoriTuberVideos.channel].value,
                        relativeRelationshipPoints
                    )
                )
                /* LoriTuberViewerChannelRelationships.upsert(
                    LoriTuberViewerChannelRelationships.owner,
                    LoriTuberViewerChannelRelationships.channel
                ) {
                    it[LoriTuberViewerChannelRelationships.owner] = viewer[LoriTuberViewers.id]
                    it[LoriTuberViewerChannelRelationships.channel] = bestVideoForMeToWatch[LoriTuberVideos.channel]
                    it[LoriTuberViewerChannelRelationships.relationshipPoints] =
                        myRelationshipPointsWithTheChannel + relativeRelationshipPoints
                } */

                if (0 > relativeRelationshipPoints) {
                    newViewerDislikes.add(
                        ViewerDislike(
                            viewer[LoriTuberViewers.id].value,
                            bestVideoForMeToWatch[LoriTuberVideos.id].value,
                            currentTick
                        )
                    )

                    /* LoriTuberViewerDislikes.insert {
                        it[LoriTuberViewerDislikes.owner] = viewer[LoriTuberViewers.id]
                        it[LoriTuberViewerDislikes.video] = bestVideoForMeToWatch[LoriTuberVideos.id]
                        it[LoriTuberViewerDislikes.dislikedAt] = now
                        it[LoriTuberViewerDislikes.dislikedAtTicks] = currentTick

                        // Store a snapshot of the vibes
                        it[LoriTuberViewerDislikes.vibe1] = vibe1
                        it[LoriTuberViewerDislikes.vibe2] = vibe2
                        it[LoriTuberViewerDislikes.vibe3] = vibe3
                        it[LoriTuberViewerDislikes.vibe4] = vibe4
                        it[LoriTuberViewerDislikes.vibe5] = vibe5
                        it[LoriTuberViewerDislikes.vibe6] = vibe6
                        it[LoriTuberViewerDislikes.vibe7] = vibe7
                    } */

                    // println("${bestVideoForMeToWatch[LoriTuberVideos.id]} I do not vibe with this video! ${viewer[LoriTuberViewers.handle]}")
                } else if (relativeRelationshipPoints > 0) {
                    newViewerLikes.add(
                        ViewerLike(
                            viewer[LoriTuberViewers.id].value,
                            bestVideoForMeToWatch[LoriTuberVideos.id].value,
                            currentTick
                        )
                    )

                    /* LoriTuberViewerLikes.insert {
                        it[LoriTuberViewerLikes.owner] = viewer[LoriTuberViewers.id]
                        it[LoriTuberViewerLikes.video] = bestVideoForMeToWatch[LoriTuberVideos.id]
                        it[LoriTuberViewerLikes.likedAt] = now
                        it[LoriTuberViewerLikes.likedAtTicks] = currentTick

                        // Store a snapshot of the vibes
                        it[LoriTuberViewerLikes.vibe1] = vibe1
                        it[LoriTuberViewerLikes.vibe2] = vibe2
                        it[LoriTuberViewerLikes.vibe3] = vibe3
                        it[LoriTuberViewerLikes.vibe4] = vibe4
                        it[LoriTuberViewerLikes.vibe5] = vibe5
                        it[LoriTuberViewerLikes.vibe6] = vibe6
                        it[LoriTuberViewerLikes.vibe7] = vibe7
                    } */
                    // println("${bestVideoForMeToWatch[LoriTuberVideos.id]} I vibe with this video! ${viewer[LoriTuberViewers.handle]}")
                }
            }

            // Now we process everything with batch inserts
            LoriTuberViewerViews.batchInsert(newViewerViews, shouldReturnGeneratedValues = false) {
                this[LoriTuberViewerViews.owner] = it.viewerId
                this[LoriTuberViewerViews.video] = it.videoId
                this[LoriTuberViewerViews.viewedAtTicks] = it.currentTick
                this[LoriTuberViewerViews.viewedAt] = Instant.now() // TODO: Fix this!
            }

            LoriTuberViewerLikes.batchInsert(newViewerLikes, shouldReturnGeneratedValues = false) {
                this[LoriTuberViewerLikes.owner] = it.viewerId
                this[LoriTuberViewerLikes.video] = it.videoId
                this[LoriTuberViewerLikes.likedAtTicks] = it.currentTick
                this[LoriTuberViewerLikes.likedAt] = Instant.now() // TODO: Fix this!
                this[LoriTuberViewerLikes.vibe1] = -1
                this[LoriTuberViewerLikes.vibe2] = -1
                this[LoriTuberViewerLikes.vibe3] = -1
                this[LoriTuberViewerLikes.vibe4] = -1
                this[LoriTuberViewerLikes.vibe5] = -1
                this[LoriTuberViewerLikes.vibe6] = -1
                this[LoriTuberViewerLikes.vibe7] = -1
            }

            LoriTuberViewerDislikes.batchInsert(newViewerDislikes, shouldReturnGeneratedValues = false) {
                this[LoriTuberViewerDislikes.owner] = it.viewerId
                this[LoriTuberViewerDislikes.video] = it.videoId
                this[LoriTuberViewerDislikes.dislikedAtTicks] = it.currentTick
                this[LoriTuberViewerDislikes.dislikedAt] = Instant.now() // TODO: Fix this!
                this[LoriTuberViewerDislikes.vibe1] = -1
                this[LoriTuberViewerDislikes.vibe2] = -1
                this[LoriTuberViewerDislikes.vibe3] = -1
                this[LoriTuberViewerDislikes.vibe4] = -1
                this[LoriTuberViewerDislikes.vibe5] = -1
                this[LoriTuberViewerDislikes.vibe6] = -1
                this[LoriTuberViewerDislikes.vibe7] = -1
            }

            // Relationship changes are a bit trickier, we need to group by channelId + new rel score
            // TODO: This doesn't work because we also need to insert them if they aren't present, FUCK
            /* newRelationshipChanges
                .groupBy { it.channelId }
                .forEach { (channelId, data) ->
                    data.groupBy { it.relationshipChange }.forEach { (relChange, innerData) ->
                        LoriTuberViewerChannelRelationships.update({ LoriTuberViewerChannelRelationships.channel eq channelId }) {
                            with(SqlExpressionBuilder) {
                                it[LoriTuberViewerChannelRelationships.relationshipPoints] = LoriTuberViewerChannelRelationships.relationshipPoints + relChange
                            }
                        }
                    }
                } */

            println("tickVideos: ${System.currentTimeMillis() - tickVideos}ms")
        } else {
            println("Skipping tickVideos...")
        }

        logger.info { "Processing LoriTuber viewer decay..." }

        val channelsToBeDecayed = LoriTuberCharacters
            .innerJoin(LoriTuberChannels)
            .select(LoriTuberChannels.id)
            .where {
                // We check by ticksLived to only process users that aren't offline
                // We decay every 1 in game day
                LoriTuberCharacters.ticksLived.mod(1_440) eq 0
            }
            .map { it[LoriTuberChannels.id] }

        logger.info { "Decaying relationships of channels ${channelsToBeDecayed}" }

        // Positive relations decay faster than bad relations
        // Because when you hate something, you REALLY hate something
        // TODO: Can't we use max/min?
        LoriTuberViewerChannelRelationships.update({
            LoriTuberViewerChannelRelationships.channel inList channelsToBeDecayed and (LoriTuberViewerChannelRelationships.relationshipPoints greater 1)
        }) {
            with(SqlExpressionBuilder) {
                it[LoriTuberViewerChannelRelationships.relationshipPoints] = LoriTuberViewerChannelRelationships.relationshipPoints - 2
            }
        }

        // Same thing as before but we do it like this to avoid a previously good relationship falling into -1
        LoriTuberViewerChannelRelationships.update({
            LoriTuberViewerChannelRelationships.channel inList channelsToBeDecayed and (LoriTuberViewerChannelRelationships.relationshipPoints eq 1)
        }) {
            with(SqlExpressionBuilder) {
                it[LoriTuberViewerChannelRelationships.relationshipPoints] = LoriTuberViewerChannelRelationships.relationshipPoints - 1
            }
        }

        LoriTuberViewerChannelRelationships.update({
            LoriTuberViewerChannelRelationships.channel inList channelsToBeDecayed and (LoriTuberViewerChannelRelationships.relationshipPoints less 0)
        }) {
            with(SqlExpressionBuilder) {
                it[LoriTuberViewerChannelRelationships.relationshipPoints] = LoriTuberViewerChannelRelationships.relationshipPoints + 1
            }
        }

        // Trend shifts
        logger.info { "Processing LoriTuber viewer trend shifts..." }
        val viewersThatCanBeBeTrendShifted = LoriTuberViewers
            .leftJoin(LoriTuberViewerChannelRelationships, { LoriTuberViewers.id }, { LoriTuberViewerChannelRelationships.owner })
            .select(LoriTuberViewers.id)
            .groupBy(LoriTuberViewers.id)
            .having {
                LoriTuberViewerChannelRelationships.relationshipPoints.max().isNull() or (LoriTuberViewerChannelRelationships.relationshipPoints.max() less 80)
            }
            .toList()

        // For each trender
        // logger.info { "Trend shifting viewers: ${viewersThatCanBeBeTrendShifted.map { it[LoriTuberViewers.id] }}" }
    }

    fun getVibeScore(myPreferencesOfThisCategory: Long, bitPosition: Int) = if ((myPreferencesOfThisCategory and (1L shl bitPosition)) == 1L) {
        1
    } else -1

    data class ViewerLike(
        val viewerId: Long,
        val videoId: Long,
        val currentTick: Long
    )

    data class ViewerDislike(
        val viewerId: Long,
        val videoId: Long,
        val currentTick: Long
    )

    data class ViewerView(
        val viewerId: Long,
        val videoId: Long,
        val currentTick: Long
    )

    data class ViewerRelationshipChange(
        val viewerId: Long,
        val channelId: Long,
        val relationshipChange: Int
    )
}