package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class SetCharacterTakingAShowerRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
) : LoriTuberRequest()

@Serializable
data object SetCharacterTakingAShowerResponse : LoriTuberResponse()
