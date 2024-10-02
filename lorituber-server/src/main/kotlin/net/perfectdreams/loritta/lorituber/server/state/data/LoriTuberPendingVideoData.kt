package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class LoriTuberPendingVideoData(
    var id: Long,
    var contentCategory: LoriTuberVideoContentCategory,
    var vibes: LoriTuberVibes,

    // The current development stage of the video
    // var currentStage: LoriTuberVideoStage,

    var contentStage: LoriTuberPendingVideoStageData,
    var recordingStage: LoriTuberPendingVideoStageData,
    var editingStage: LoriTuberPendingVideoStageData,
    var renderingStage: LoriTuberPendingVideoStageData,
    var thumbnailStage: LoriTuberPendingVideoStageData,

    // The current stage progress in ticks
    // var currentStageProgressTicks: Long,

    // var recordingScore: Int?,
    // var editingScore: Int?,
    // var thumbnailScore: Int?,
    // var videoResolution: LoriTuberVideoResolution?
)