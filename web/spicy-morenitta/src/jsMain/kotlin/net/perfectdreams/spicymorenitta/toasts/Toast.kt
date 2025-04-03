package net.perfectdreams.spicymorenitta.toasts

import react.dom.html.HTMLAttributes
import web.html.HTMLDivElement

data class Toast(
    val type: Type,
    val title: String,
    val body: HTMLAttributes<HTMLDivElement>.() -> (Unit)
) {
    enum class Type {
        INFO,
        SUCCESS,
        WARN
    }
}