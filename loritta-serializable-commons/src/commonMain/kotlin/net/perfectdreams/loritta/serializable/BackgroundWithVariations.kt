package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundWithVariations(
    val background: Background,
    val variations: List<BackgroundVariation>
)