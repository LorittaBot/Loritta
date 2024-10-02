package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class ViewCharacterMotivesRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data class ViewCharacterMotivesResponse(
    val currentTick: Long,
    val name: String,
    val energyNeed: Double,
    val hungerNeed: Double,
    val funNeed: Double,
    val hygieneNeed: Double,
    val bladderNeed: Double,
    val socialNeed: Double,
    val currentTask: LoriTuberTask?
) : LoriTuberResponse()
