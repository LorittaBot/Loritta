package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundWithVariations

@Serializable
data class DailyShopBackgroundEntry(
    val backgroundWithVariations: BackgroundWithVariations,
    val tag: String? = null
)