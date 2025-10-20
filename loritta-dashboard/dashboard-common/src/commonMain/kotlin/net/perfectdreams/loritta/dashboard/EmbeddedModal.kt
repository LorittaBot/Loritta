package net.perfectdreams.loritta.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedModal(
    val title: String,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val bodyHtml: String,
    val buttonsHtml: List<String>
)