package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoData

object NetworkConverter {
    fun toNetwork(pendingVideo: LoriTuberPendingVideoData): NetworkLoriTuberPendingVideo {
        return NetworkLoriTuberPendingVideo(
            pendingVideo.id,
            pendingVideo.contentCategory,
            pendingVideo.contentLength,
            pendingVideo.currentStage,
            pendingVideo.currentStageProgressTicks,
            pendingVideo.recordingScore,
            pendingVideo.editingScore,
            pendingVideo.thumbnailScore,
            pendingVideo.videoResolution
        )
    }
}