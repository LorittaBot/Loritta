package net.perfectdreams.loritta.lorituber.items

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberGroceryItemData(
    val item: LoriTuberItemId,
    var inStock: Int
)