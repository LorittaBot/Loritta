package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedSpicyToast(
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