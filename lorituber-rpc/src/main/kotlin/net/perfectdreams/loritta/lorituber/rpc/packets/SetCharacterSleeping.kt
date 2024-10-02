package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class SetCharacterSleepingRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data object SetCharacterSleepingResponse : LoriTuberResponse()
