package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId

@Serializable
data class StartCraftingRequest(val characterId: Long, val items: List<LoriTuberItemId>) : LoriTuberRequest()

@Serializable
data object StartCraftingResponse : LoriTuberResponse()