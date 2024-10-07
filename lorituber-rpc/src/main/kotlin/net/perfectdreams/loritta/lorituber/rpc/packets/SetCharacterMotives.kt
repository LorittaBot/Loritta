package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class SetCharacterMotivesRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    val energyNeed: Double,
    val hungerNeed: Double,
    val funNeed: Double,
    val hygieneNeed: Double,
    val bladderNeed: Double,
    val socialNeed: Double,
) : LoriTuberRequest()

@Serializable
data object SetCharacterMotivesResponse : LoriTuberResponse()
