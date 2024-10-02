package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel

@Serializable
data class GetChannelByIdRequest(
    val channelId: Long
) : LoriTuberRequest()

@Serializable
sealed class GetChannelByIdResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        val channel: NetworkLoriTuberChannel,
    ) : GetChannelByIdResponse()

    @Serializable
    data object UnknownChannel : GetChannelByIdResponse()
}