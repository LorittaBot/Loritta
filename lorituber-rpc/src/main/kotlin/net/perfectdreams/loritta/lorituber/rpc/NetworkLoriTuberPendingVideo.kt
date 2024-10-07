package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class NetworkLoriTuberPendingVideo(
    var id: Long,
    var contentCategory: LoriTuberVideoContentCategory,

    var contentStage: LoriTuberPendingVideoStageData,
    var recordingStage: LoriTuberPendingVideoStageData,
    var editingStage: LoriTuberPendingVideoStageData,
    var renderingStage: LoriTuberPendingVideoStageData,
    var thumbnailStage: LoriTuberPendingVideoStageData,
) {
    @Serializable
    sealed class LoriTuberPendingVideoStageData {
        @Serializable
        data object Unavailable : LoriTuberPendingVideoStageData()

        @Serializable
        data class InProgress(val progressTicks: Long) : LoriTuberPendingVideoStageData()

        @Serializable
        data class Finished(val score: Int) : LoriTuberPendingVideoStageData()
    }
}