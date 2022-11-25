package utils

import org.w3c.dom.HTMLElement

@JsName("grecaptcha")
external object GoogleRecaptcha {
	fun render(element: HTMLElement, options: RecaptchaOptions)

	fun execute()
}

external interface RecaptchaOptions {
	var sitekey: String
	var callback: String
	var size: String
}