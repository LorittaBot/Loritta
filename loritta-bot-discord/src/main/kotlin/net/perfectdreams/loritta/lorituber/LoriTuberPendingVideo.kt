package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoResolution
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoStage

@Serializable
data class LoriTuberPendingVideo(
    val id: Long,
    val contentGenre: LoriTuberVideoContentCategory,
    val contentLength: LoriTuberContentLength,
    val currentStage: LoriTuberVideoStage,
    val currentStageProgressInTicks: Long,
    val recordingScore: Int?,
    val editingScore: Int?,
    val thumbnailScore: Int?,
    val videoResolution: LoriTuberVideoResolution?
)