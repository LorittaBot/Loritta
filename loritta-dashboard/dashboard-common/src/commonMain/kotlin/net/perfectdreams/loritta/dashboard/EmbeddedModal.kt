package net.perfectdreams.loritta.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedModal(
    val title: String,
    val size: Size,
    val canBeClosedByClickingOutsideTheWindow: Boolean,
    val bodyHtml: String,
    val buttonsHtml: List<String>
) {
    enum class Size {
        MEDIUM,
        LARGE
    }
}