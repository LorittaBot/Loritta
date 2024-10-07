package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberVideo
import java.util.*

@Serializable
data class GetChannelVideosRequest(
    @Serializable(UUIDSerializer::class)
    val channelId: UUID,
) : LoriTuberRequest()

@Serializable
sealed class GetChannelVideosResponse : LoriTuberResponse() {
    @Serializable
    data class Success(
        val pendingVideo: List<NetworkLoriTuberVideo>,
    ) : GetChannelVideosResponse()
}