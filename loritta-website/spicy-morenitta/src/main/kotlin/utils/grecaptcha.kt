package utils

import org.w3c.dom.HTMLElement

@JsName("grecaptcha")
external object GoogleRecaptcha {
	fun render(element: HTMLElement, options: RecaptchaOptions)

	fun execute()
}

class RecaptchaOptions(val sitekey: String, val callback: String, val size: String)