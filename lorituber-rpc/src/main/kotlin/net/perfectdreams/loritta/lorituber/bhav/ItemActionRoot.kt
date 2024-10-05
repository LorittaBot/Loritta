package net.perfectdreams.loritta.lorituber.bhav

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData

@Serializable
data class ItemActionRoot(
    val item: LoriTuberItemStackData,
    val actionOptions: List<ItemActionOption>
)