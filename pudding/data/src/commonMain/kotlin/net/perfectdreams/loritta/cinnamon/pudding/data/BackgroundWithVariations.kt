package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundWithVariations(
    val background: Background,
    val variations: List<BackgroundVariation>
)