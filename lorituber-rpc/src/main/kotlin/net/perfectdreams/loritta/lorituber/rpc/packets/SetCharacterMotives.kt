package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class SetCharacterMotivesRequest(
    val characterId: Long,
    val energyNeed: Double,
    val hungerNeed: Double,
    val funNeed: Double,
    val hygieneNeed: Double,
    val bladderNeed: Double,
    val socialNeed: Double,
) : LoriTuberRequest()

@Serializable
data object SetCharacterMotivesResponse : LoriTuberResponse()
