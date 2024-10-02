package net.perfectdreams.loritta.lorituber.recipes

import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId

data class LoriTuberRecipe(
    val id: String,
    val targetItemId: LoriTuberItemId,
    val requiredItemIds: List<LoriTuberItemId>,
    val ticks: Long
)