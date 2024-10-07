package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel
import java.util.*

@Serializable
data class GetChannelsByCharacterRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
) : LoriTuberRequest()

@Serializable
data class GetChannelsByCharacterResponse(
    val channels: List<NetworkLoriTuberChannel>
) : LoriTuberResponse()
