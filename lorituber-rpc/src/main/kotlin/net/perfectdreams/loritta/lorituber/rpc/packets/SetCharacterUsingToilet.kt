package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class SetCharacterUsingToiletRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data object SetCharacterUsingToiletResponse : LoriTuberResponse()
