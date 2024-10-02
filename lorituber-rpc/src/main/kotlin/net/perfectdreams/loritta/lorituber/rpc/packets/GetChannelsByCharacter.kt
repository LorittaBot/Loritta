package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel

@Serializable
data class GetChannelsByCharacterRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data class GetChannelsByCharacterResponse(
    val channels: List<NetworkLoriTuberChannel>
) : LoriTuberResponse()
