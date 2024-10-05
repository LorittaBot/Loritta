package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class SetCharacterTakingAShowerRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data object SetCharacterTakingAShowerResponse : LoriTuberResponse()
