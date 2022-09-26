package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.HTMLElement
import utils.GoogleRecaptcha
import utils.RecaptchaOptions

object GoogleRecaptchaUtils {
	private var isGoogleRecaptchaLoaded = false
	private val queuedRecaptchas = mutableListOf<QueuedRecaptcha>()

	@JsName("onRecaptchaLoadCallback")
	fun onRecaptchaLoadCallback() {
		isGoogleRecaptchaLoaded = true

		println("Google Recaptcha successfully loaded!")

		println("There are ${queuedRecaptchas.size} recaptchas pending")
		queuedRecaptchas.forEach {
			GoogleRecaptcha.render(it.element, it.options)
		}
		queuedRecaptchas.clear()
	}

	fun render(element: HTMLElement, options: RecaptchaOptions) {
		if (isGoogleRecaptchaLoaded) {
			println("Google Recaptcha is already loaded, rendering right now...")
			// Não precisamos colocar na fila se o recaptcha já está carregado
			GoogleRecaptcha.render(element, options)
		} else {
			println("Google Recaptcha isn't loaded yet, rendering later,..")
			queuedRecaptchas.add(QueuedRecaptcha(element, options))
		}
	}

	private data class QueuedRecaptcha(val element: HTMLElement, val options: RecaptchaOptions)
}