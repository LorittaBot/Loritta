package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData

@Serializable
data class PrepareCraftingRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
data class PrepareCraftingResponse(
    val inventory: List<LoriTuberItemStackData>
) : LoriTuberResponse()
