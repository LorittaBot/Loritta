package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import java.util.*

@Serializable
data class EatFoodRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    val itemId: LoriTuberItemId
) : LoriTuberRequest()

@Serializable
sealed class EatFoodResponse : LoriTuberResponse() {
    @Serializable
    data object ItemNotEdible : EatFoodResponse()
    @Serializable
    data object ItemNotFound : EatFoodResponse()
    @Serializable
    data object Success : EatFoodResponse()
}
