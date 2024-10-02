package net.perfectdreams.loritta.lorituber.items

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberItem(
    val id: LoriTuberItemId,
    val price: Long,
    val foodAttributes: FoodAttributes?
) {
    @Serializable
    data class FoodAttributes(
        val hunger: Long,
        val ticks: Long
    )
}