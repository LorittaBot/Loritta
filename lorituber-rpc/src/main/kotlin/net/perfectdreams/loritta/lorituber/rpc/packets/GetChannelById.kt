package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel
import java.util.*

@Serializable
data class GetChannelByIdRequest(
    @Serializable(UUIDSerializer::class)
    val channelId: UUID,
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