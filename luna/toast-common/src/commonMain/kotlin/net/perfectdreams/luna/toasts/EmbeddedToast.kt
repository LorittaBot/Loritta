package net.perfectdreams.luna.toasts

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedToast(
    val type: Type,
    val title: String,
    val descriptionHtml: String?
) {
    enum class Type {
        INFO,
        SUCCESS,
        WARN
    }
}