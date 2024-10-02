package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class FinishPendingVideoRequest(
    val channelId: Long,
    val pendingVideoId: Long,
    val videoTitle: String
) : LoriTuberRequest()

@Serializable
sealed class FinishPendingVideoResponse : LoriTuberResponse() {
    @Serializable
    data object Success : FinishPendingVideoResponse()

    @Serializable
    data object UnknownChannel : FinishPendingVideoResponse()

    @Serializable
    data object UnknownPendingVideo : FinishPendingVideoResponse()
}