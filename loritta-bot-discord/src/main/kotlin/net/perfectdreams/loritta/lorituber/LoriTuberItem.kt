package net.perfectdreams.loritta.lorituber

data class LoriTuberItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
    val foodAttributes: FoodAttributes?
) {
    data class FoodAttributes(
        val hunger: Long,
        val ticks: Long
    )
}