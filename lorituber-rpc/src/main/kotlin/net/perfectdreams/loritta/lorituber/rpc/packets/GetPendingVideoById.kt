package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberPendingVideo

@Serializable
data class GetPendingVideoByIdRequest(
    val channelId: Long,
    val pendingVideoId: Long
) : LoriTuberRequest()

@Serializable
sealed class GetPendingVideoByIdResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        val pendingVideo: NetworkLoriTuberPendingVideo,
    ) : GetPendingVideoByIdResponse()

    @Serializable
    data object UnknownChannel : GetPendingVideoByIdResponse()

    @Serializable
    data object UnknownPendingVideo : GetPendingVideoByIdResponse()
}