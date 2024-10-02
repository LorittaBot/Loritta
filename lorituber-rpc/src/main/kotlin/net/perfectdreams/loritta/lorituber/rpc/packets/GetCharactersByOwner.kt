package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberCharacter

@Serializable
data class GetCharactersByOwnerRequest(val userId: Long) : LoriTuberRequest()

@Serializable
data class GetCharactersByOwnerResponse(
    val characters: List<NetworkLoriTuberCharacter>
) : LoriTuberResponse()
