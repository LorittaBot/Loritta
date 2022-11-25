@file:JsExport
@file:NoLiveLiterals
package net.perfectdreams.spicymorenitta.utils

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.append
import kotlinx.html.js.script
import org.w3c.dom.HTMLModElement
import org.w3c.dom.get
import kotlin.random.Random

object NitroPay : Logging {
	val vidoomyABTest = Random.nextBoolean()
	var isVidoomyLoaded = false

	@JsName("renderAds")
	fun renderAds() {
		println("Vidoomy x NitroPay Ads A/B Test: $vidoomyABTest")
		if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
			println("NitroPay is loaded!")
			renderNitroPayAds()
		} else {
			println("NitroPay is not loaded yet! We are going to wait until the event is triggered to render the ads...")
			document.addEventListener("nitroAds.loaded", {
				// nitroAds just loaded
				renderNitroPayAds()
			})
		}
	}

	private fun renderNitroPayAds() {
		val ads = document.selectAll<HTMLModElement>(".nitropay-ad")

		println("There are ${ads.size} NitroPay ads in the page...")

		for (it in ads) {
			if (!it.hasAttribute("data-request-id")) {
				try {
					console.log(it)

					val adType = it.getAttribute("data-nitropay-ad-type")

					val dynamic = object {}.asDynamic()

					// Enable demo mode if we aren't running it in a real website
					if (!window.location.hostname.contains("loritta.website"))
						dynamic.demo = true

					if (adType == "video_player") {
						if (vidoomyABTest) {
							// Load Vidoomy as an A/B test
							if (!isVidoomyLoaded) {
								println("Loading Vidoomy's script instead of NitroPay's video ad...")
								// <script type="text/javascript" src="https://ads.vidoomy.com/lorittawebsite_18569.js" async></script>
								document.head?.append {
									script(
										type = "text/javascript",
										src = "https://ads.vidoomy.com/lorittawebsite_18569.js"
									) {
										attributes["async"] = "true"
									}
								}
								isVidoomyLoaded = true
							}
							continue
						} else {
							// If else, we are going to load NitroPay instead
							dynamic.format = "video-ac"
						}
					} else {
						dynamic.refreshLimit = 10
						dynamic.refreshTime = 30
						// Lazy loading
						dynamic.renderVisibleOnly = false
						dynamic.refreshVisibleOnly = true

						val mediaQuery = it.getAttribute("data-nitropay-media-query")

						if (mediaQuery != null)
							dynamic.mediaQuery = mediaQuery

						val adSizes = it.getAttribute("data-nitropay-ad-sizes")!!.split(", ")

						dynamic.sizes = adSizes.map {
							val (width, height) = it.split("x")
							arrayOf(
								width,
								height
							)
						}.toTypedArray()

						// While the report button is cool, it affects our ad container wrapper :(
						// val report = object {}.asDynamic()
						// report.enabled = true
						// report.wording = "Report Ad"
						// report.position = "top-right"
						// dynamic.report = report

						console.log(dynamic)
					}

					window["nitroAds"].createAd(it.id, dynamic)

					println("Yay!")
				} catch (e: Throwable) {
					console.log(e)
				}
			}
		}
	}
}