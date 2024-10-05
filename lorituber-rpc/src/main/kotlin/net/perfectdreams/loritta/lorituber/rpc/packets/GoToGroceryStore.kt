package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData

@Serializable
data class GoToGroceryStoreRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
sealed class GoToGroceryStoreResponse : LoriTuberResponse() {
    @Serializable
    data object Closed : GoToGroceryStoreResponse()

    @Serializable
    data class Success(
        val characterSonhos: Long,
        val items: List<LoriTuberGroceryItemData>
    ) : GoToGroceryStoreResponse()
}