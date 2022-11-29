package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.HTMLElement

@JsExport
object CloudflareTurnstileUtils {
    private var isCloudflareTurnstileLoaded = false
    private val queuedRecaptchas = mutableListOf<QueuedRecaptcha>()


    @JsName("onRecaptchaLoadCallback")
    fun onRecaptchaLoadCallback() {
        isCloudflareTurnstileLoaded = true

        println("Cloudflare Turnstile successfully loaded!")

        println("There are ${queuedRecaptchas.size} recaptchas pending")
        queuedRecaptchas.forEach {
            CloudflareTurnstile.render(it.element, it.options)
        }
        queuedRecaptchas.clear()
    }

    fun render(element: HTMLElement, options: TurnstileOptions) {
        if (isCloudflareTurnstileLoaded) {
            println("Cloudflare Turnstile is already loaded, rendering right now...")
            // Não precisamos colocar na fila se o recaptcha já está carregado
            CloudflareTurnstile.render(element, options)
        } else {
            println("Cloudflare Turnstile isn't loaded yet, rendering later,..")
            queuedRecaptchas.add(QueuedRecaptcha(element, options))
        }
    }

    private data class QueuedRecaptcha(val element: HTMLElement, val options: TurnstileOptions)
}