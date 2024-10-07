package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import java.util.*

@Serializable
data class SelectFoodMenuRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID
) : LoriTuberRequest()

@Serializable
data class SelectFoodMenuResponse(
    val inventory: List<LoriTuberItemStackData>
) : LoriTuberResponse()
