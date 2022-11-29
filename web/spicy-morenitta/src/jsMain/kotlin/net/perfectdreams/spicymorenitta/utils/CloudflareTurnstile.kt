package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.HTMLElement

@JsName("turnstile")
external object CloudflareTurnstile {
    fun render(element: HTMLElement, options: TurnstileOptions)

    fun execute()
}

external interface TurnstileOptions {
    var sitekey: String
    var callback: (String) -> (Unit)
    var size: String
}