package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedSpicyModal(
    val title: String,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val bodyHtml: String,
    val buttonsHtml: List<String>
)