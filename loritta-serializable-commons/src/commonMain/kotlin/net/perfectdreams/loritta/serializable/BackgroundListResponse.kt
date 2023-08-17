package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.BackgroundWithVariations

@Serializable
data class BackgroundListResponse(
    val dreamStorageServiceUrl: String,
    val namespace: String,
    val etherealGambiUrl: String,
    val backgroundsWithVariations: List<BackgroundWithVariations>
)