package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberPendingVideoStageData {
    @Serializable
    data object Unavailable : LoriTuberPendingVideoStageData()

    @Serializable
    data class InProgress(var progressTicks: Long) : LoriTuberPendingVideoStageData()

    @Serializable
    data class Finished(var score: Int) : LoriTuberPendingVideoStageData()
}