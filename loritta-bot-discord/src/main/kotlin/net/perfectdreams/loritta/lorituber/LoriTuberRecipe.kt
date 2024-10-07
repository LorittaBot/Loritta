package net.perfectdreams.loritta.lorituber

data class LoriTuberRecipe(
    val id: String,
    val targetItemId: String,
    val requiredItemIds: List<String>,
    val ticks: Long
)