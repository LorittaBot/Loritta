package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoResolution
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage

@Serializable
data class LoriTuberPendingVideoData(
    var id: Long,
    var contentCategory: LoriTuberVideoContentCategory,
    var contentLength: LoriTuberContentLength,

    // The current development stage of the video
    var currentStage: LoriTuberVideoStage,

    // The current stage progress in ticks
    var currentStageProgressTicks: Long,

    var recordingScore: Int?,
    var editingScore: Int?,
    var thumbnailScore: Int?,
    var videoResolution: LoriTuberVideoResolution?
)