package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import kotlin.browser.window
import kotlin.js.Json

object PaymentUtils : Logging {
	/**
	 * Sends a payment request to Loritta and, if valid, redirects the user
	 */
	fun requestAndRedirectToPaymentUrl(meta: dynamic, url: String = "${loriUrl}api/v1/users/donate") {
		debug("Requesting a PerfectPayments payment URL...")
		println(JSON.stringify(meta))

		SpicyMorenitta.INSTANCE.showLoadingScreen()

		GlobalScope.launch {
			val response = HttpRequest.post(url, JSON.stringify(meta))

			val payload = JSON.parse<Json>(response.body)
			window.location.href = payload["redirectUrl"] as String
		}
	}
}