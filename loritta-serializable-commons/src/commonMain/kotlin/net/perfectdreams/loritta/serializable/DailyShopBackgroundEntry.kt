package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.BackgroundWithVariations

@Serializable
data class DailyShopBackgroundEntry(
    val backgroundWithVariations: BackgroundWithVariations,
    val tag: String? = null
)