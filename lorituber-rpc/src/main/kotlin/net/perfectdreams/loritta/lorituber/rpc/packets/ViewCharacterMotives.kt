package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.bhav.ItemActionRoot
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData

@Serializable
data class ViewCharacterMotivesRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data class ViewCharacterMotivesResponse(
    val currentTick: Long,
    val name: String,
    val sonhos: Long,
    val mood: Double,
    val energyNeed: Double,
    val hungerNeed: Double,
    val funNeed: Double,
    val hygieneNeed: Double,
    val bladderNeed: Double,
    val socialNeed: Double,
    val currentTask: LoriTuberTask?,
    val items: List<LoriTuberItemStackData>,
    val itemActions: List<ItemActionRoot>
) : LoriTuberResponse()