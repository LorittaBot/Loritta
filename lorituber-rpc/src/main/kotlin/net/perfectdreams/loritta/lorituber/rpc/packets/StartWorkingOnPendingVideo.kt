package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage

@Serializable
data class StartWorkingOnPendingVideoRequest(val characterId: Long, val channelId: Long, val pendingVideoId: Long, val stage: LoriTuberVideoStage) : LoriTuberRequest()

@Serializable
sealed class StartWorkingOnPendingVideoResponse : LoriTuberResponse() {
    @Serializable
    data object Success : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object MoodTooLow : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object UnknownChannel : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object UnknownPendingVideo : StartWorkingOnPendingVideoResponse()
}