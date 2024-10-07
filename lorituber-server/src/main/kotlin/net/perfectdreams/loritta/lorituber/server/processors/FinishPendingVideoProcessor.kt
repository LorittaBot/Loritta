package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LoriTuberVideoCommentType
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.rpc.packets.FinishPendingVideoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.FinishPendingVideoResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer.Companion.ONE_DAY_IN_TICKS
import net.perfectdreams.loritta.lorituber.server.easeInQuad
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoStageData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoCommentData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoEvent
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberVideo
import kotlin.time.Duration.Companion.milliseconds

class FinishPendingVideoProcessor(val m: LoriTuberServer) : PacketProcessor<FinishPendingVideoRequest> {
    override suspend fun process(request: FinishPendingVideoRequest): LoriTuberResponse {
        val channel = m.gameState.channels.firstOrNull {
            it.id == request.channelId
        }

        if (channel == null)
            return FinishPendingVideoResponse.UnknownChannel

        val pendingVideo = channel.data.pendingVideos.firstOrNull { it.id == request.pendingVideoId }
        if (pendingVideo == null)
            return FinishPendingVideoResponse.UnknownPendingVideo

        val character = m.gameState.characters.first { it.id == channel.data.characterId }

        // Remove the pending video from our list
        channel.data.pendingVideos.removeIf { it.id == pendingVideo.id }

        // Add the new video!
        val videoEvents = mutableMapOf<Long, List<LoriTuberVideoEvent>>()

        val vibesOfTheCurrentCategory = m.gameState.trendsByCategory[pendingVideo.contentCategory]!!
        val comments = mutableListOf<LoriTuberVideoCommentData>()

        val video = LoriTuberVideo(
            m.gameState.generateVideoId(),
            LoriTuberVideoData(
                channel.id,
                request.videoTitle,
                true,
                m.gameState.worldInfo.currentTick,
                pendingVideo.contentCategory,
                (pendingVideo.contentStage as LoriTuberPendingVideoStageData.Finished).score,
                (pendingVideo.recordingStage as LoriTuberPendingVideoStageData.Finished).score,
                (pendingVideo.editingStage as LoriTuberPendingVideoStageData.Finished).score,
                (pendingVideo.thumbnailStage as LoriTuberPendingVideoStageData.Finished).score,
                pendingVideo.vibes,
                vibesOfTheCurrentCategory.vibes.copy(),
                0,
                0,
                0,
                videoEvents,
                comments
            )
        )

        val startX = System.currentTimeMillis()

        val videoGameplayScore = (video.data.contentScore + video.data.recordingScore + video.data.editingScore + video.data.thumbnailScore) / 4
        val videoGameplayProgress = videoGameplayScore / 999.0

        var matchedVibes = 0
        for (vibe in LoriTuberVideoContentVibes.entries) {
            if (vibesOfTheCurrentCategory.vibes.vibeType(vibe) == video.data.vibes.vibeType(vibe)) {
                matchedVibes++
            }
        }

        var targetLikes = 0
        var targetDislikes = 0
        var targetComments = 0

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
        val targetViews = subViews + (baseViews * easeInQuad(videoGameplayProgress)).toInt()

        repeat(targetViews) {
            val shouldILike = m.gameState.random.nextFloat()
            val shouldIComment = m.gameState.random.nextFloat()

            // The likes and dislikes are actually NOT just cosmetic! They can be used to figure out if the vibes are good or not
            if (chancesOfLiking > shouldILike)
                targetLikes++
            else if ((chancesOfLiking + chancesOfDisliking) > shouldILike)
                targetDislikes++

            // if (0.0055f > shouldIComment)
            if (0.01f > shouldIComment)
                targetComments++
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

                // Normal vibes do not hurt your relationship, but you also don't get punished by the daily decay
                if (matchedVibes == 3) 6 else 12

                /* when (video.data.contentLength) {
                    LoriTuberContentLength.SHORT -> {
                        4
                    }

                    LoriTuberContentLength.MEDIUM -> {
                        12
                    }

                    LoriTuberContentLength.LONG -> {
                        28
                    }
                } */
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

        var modifiedTargetComments = targetComments
        if (modifiedTargetComments > 0 && 0.1f > m.gameState.random.nextFloat()) {
            // First!
            comments.add(
                LoriTuberVideoCommentData(
                    60,
                    m.gameState.random.nextInt(m.gameState.viewerHandles.size),
                    LoriTuberVideoCommentType.First0,
                )
            )
            modifiedTargetComments--
        }

        var hasProvidedVibeFeedback = false

        repeat(modifiedTargetComments) {
            val whenCommentWillBePosted = m.gameState.random.nextLong(120, (ticksUntilEndOfEngagement.toLong() / 2))

            if (!hasProvidedVibeFeedback) {
                val vibeToBeFeedbacked = LoriTuberVideoContentVibes.entries.random()

                val trendVibeValue = vibesOfTheCurrentCategory.vibes.vibeType(vibeToBeFeedbacked)
                val videoVibeValue = video.data.vibes.vibeType(vibeToBeFeedbacked)

                val prefix = if (matchedVibes == 3)
                    "NeutralVideo"
                else if (matchedVibes > 3)
                    "LikedVideo"
                else
                    "DislikedVideo"

                val vibeValue = if (trendVibeValue == videoVibeValue) {
                    "CorrectVibe"
                } else "IncorrectVibe"

                val vibeId = (vibeToBeFeedbacked.ordinal + 1)

                val aligmentType = if (videoVibeValue) {
                    "AlignmentRight"
                } else {
                    "AlignmentLeft"
                }

                // WE LOVE REFLECTION!!!
                println("${prefix}${vibeValue}${vibeId}${aligmentType}0")
                val clazz = LoriTuberVideoCommentType::class.nestedClasses.first {
                    it.simpleName == "${prefix}${vibeValue}${vibeId}${aligmentType}0"
                }.objectInstance

                comments.add(
                    LoriTuberVideoCommentData(
                        whenCommentWillBePosted,
                        m.gameState.random.nextInt(m.gameState.viewerHandles.size),
                        clazz as LoriTuberVideoCommentType,
                    )
                )

                hasProvidedVibeFeedback = true
            }
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

        m.gameState.videosById[video.id] = video

        println("Result: ${video}")
        println("Matched vibes: $matchedVibes")
        println("Base Views: $baseViews")
        println("Sub Views: $subViews")
        println("Target Views: $targetViews")
        println("Target Likes: $targetLikes")
        println("Target Dislikes: $targetDislikes")
        println("Target Comments: $targetComments")

        video.isDirty = true

        // Increase our category level
        channel.addCategoryLevel(video.data.contentCategory, 1)
        channel.isDirty = true

        return FinishPendingVideoResponse.Success
    }
}