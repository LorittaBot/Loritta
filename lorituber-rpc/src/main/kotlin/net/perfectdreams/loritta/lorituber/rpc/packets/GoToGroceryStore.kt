package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import java.util.*

@Serializable
data class GoToGroceryStoreRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
) : LoriTuberRequest()

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