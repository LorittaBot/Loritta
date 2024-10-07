package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import java.util.*

@Serializable
data class StartCraftingRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    val items: List<LoriTuberItemId>
) : LoriTuberRequest()

@Serializable
data object StartCraftingResponse : LoriTuberResponse()