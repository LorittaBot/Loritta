package net.perfectdreams.spicymorenitta.utils

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLModElement
import org.w3c.dom.get

object NitroPay : Logging {
	@JsName("renderAds")
	fun renderAds() {
		if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
			renderNitroPayAds()
		} else {
			document.addEventListener("nitroAds.loaded", {
				// nitroAds just loaded
				renderNitroPayAds()
			})
		}
	}

	private fun renderNitroPayAds() {
		val ads = document.selectAll<HTMLModElement>(".nitropay-ad")

		debug("There are ${ads.size} NitroPay ads in the page...")

		ads.forEach {
			if (!it.hasAttribute("data-adsbygoogle-status")) {
				try {
					console.log(it)

					val dynamic = object {}.asDynamic()
					dynamic.refreshLimit = 10
					dynamic.refreshTime = 90
					dynamic.renderVisibleOnly = true
					dynamic.refreshVisibleOnly = true
					dynamic.sizes = arrayOf<Any?>()

					val report = object {}.asDynamic()
					report.enabled = true
					report.wording = "Report Ad"
					report.position = "top-right"
					dynamic.report = report

					console.log(dynamic)

					window["nitroAds"].createAd(it.id, dynamic)

					debug("Yay!")
				} catch (e: Throwable) {
					console.log(e)
				}
			}
	}
}