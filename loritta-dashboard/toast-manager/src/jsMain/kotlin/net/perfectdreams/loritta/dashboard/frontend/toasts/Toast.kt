package net.perfectdreams.loritta.dashboard.frontend.toasts

import kotlinx.html.HTMLTag

data class Toast(
    val type: Type,
    val title: String,
    val body: HTMLTag.() -> (Unit)
) {
    enum class Type {
        INFO,
        SUCCESS,
        WARN
    }
}