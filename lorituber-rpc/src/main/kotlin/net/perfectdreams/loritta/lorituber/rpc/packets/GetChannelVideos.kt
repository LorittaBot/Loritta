package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberVideo

@Serializable
data class GetChannelVideosRequest(
    val channelId: Long
) : LoriTuberRequest()

@Serializable
sealed class GetChannelVideosResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        val pendingVideo: List<NetworkLoriTuberVideo>,
    ) : GetChannelVideosResponse()
}