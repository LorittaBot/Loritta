package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberVideo
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberVideoComment
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoStageData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberChannel
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberVideo

object NetworkConverter {
    fun toNetwork(pendingVideo: LoriTuberPendingVideoData): NetworkLoriTuberPendingVideo {
        return NetworkLoriTuberPendingVideo(
            pendingVideo.id,
            pendingVideo.contentCategory,
            when (val stage = pendingVideo.contentStage) {
                is LoriTuberPendingVideoStageData.Finished -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished(stage.score)
                is LoriTuberPendingVideoStageData.InProgress -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress(stage.progressTicks)
                LoriTuberPendingVideoStageData.Unavailable -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable
            },
            when (val stage = pendingVideo.recordingStage) {
                is LoriTuberPendingVideoStageData.Finished -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished(stage.score)
                is LoriTuberPendingVideoStageData.InProgress -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress(stage.progressTicks)
                LoriTuberPendingVideoStageData.Unavailable -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable
            },
            when (val stage = pendingVideo.editingStage) {
                is LoriTuberPendingVideoStageData.Finished -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished(stage.score)
                is LoriTuberPendingVideoStageData.InProgress -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress(stage.progressTicks)
                LoriTuberPendingVideoStageData.Unavailable -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable
            },
            when (val stage = pendingVideo.renderingStage) {
                is LoriTuberPendingVideoStageData.Finished -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished(stage.score)
                is LoriTuberPendingVideoStageData.InProgress -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress(stage.progressTicks)
                LoriTuberPendingVideoStageData.Unavailable -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable
            },
            when (val stage = pendingVideo.thumbnailStage) {
                is LoriTuberPendingVideoStageData.Finished -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished(stage.score)
                is LoriTuberPendingVideoStageData.InProgress -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress(stage.progressTicks)
                LoriTuberPendingVideoStageData.Unavailable -> NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable
            }
        )
    }

    fun toNetwork(gameState: GameState, pendingVideo: LoriTuberVideo): NetworkLoriTuberVideo {
        return NetworkLoriTuberVideo(
            pendingVideo.id,
            pendingVideo.data.title,
            pendingVideo.data.postedAtTicks,
            pendingVideo.data.contentCategory,
            pendingVideo.data.vibes,
            LoriTuberVibes.vibeMatches(pendingVideo.data.vibes, pendingVideo.data.vibesAtTheTime),
            pendingVideo.data.recordingScore,
            pendingVideo.data.editingScore,
            pendingVideo.data.thumbnailScore,
            pendingVideo.data.views,
            pendingVideo.data.likes,
            pendingVideo.data.dislikes,
            pendingVideo.data.comments.map {
                NetworkLoriTuberVideoComment(
                    it.postedAtTicksAfterVideoPost,
                    gameState.viewerHandles[it.viewerHandleId],
                    it.commentType,
                )
            }
        )
    }

    fun toNetwork(channel: LoriTuberChannel): NetworkLoriTuberChannel {
        return NetworkLoriTuberChannel(
            channel.id,
            channel.data.name,
            channel.data.pendingVideos.map {
                NetworkConverter.toNetwork(it)
            },
            channel.data.channelRelationshipsV2.values.sumOf { it.subscribers },
            channel.data.categoryLevels,
            channel.data.channelRelationshipsV2.entries.associate {
                it.key to NetworkLoriTuberChannel.LoriTuberSuperViewerChannelRelationshipData(
                    it.value.relationshipScore,
                    it.value.subscribers
                )
            }
        )
    }
}