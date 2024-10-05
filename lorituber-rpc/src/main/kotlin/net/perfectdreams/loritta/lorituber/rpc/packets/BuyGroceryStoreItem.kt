package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId

@Serializable
data class BuyGroceryStoreItemRequest(val characterId: Long, val item: LoriTuberItemId) : LoriTuberRequest()

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