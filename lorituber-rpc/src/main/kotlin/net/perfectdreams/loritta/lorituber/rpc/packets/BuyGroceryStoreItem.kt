package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import java.util.*

@Serializable
data class BuyGroceryStoreItemRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID, val item: LoriTuberItemId
) : LoriTuberRequest()

@Serializable
sealed class BuyGroceryStoreItemResponse : LoriTuberResponse() {
    @Serializable
    data object Closed : BuyGroceryStoreItemResponse()

    @Serializable
    data object NotInStock : BuyGroceryStoreItemResponse()

    @Serializable
    data object NotEnoughSonhos : BuyGroceryStoreItemResponse()

    @Serializable
    data object Success : BuyGroceryStoreItemResponse()
}