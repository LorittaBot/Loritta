package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLModElement
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import kotlinx.browser.document

object GoogleAdSense : Logging {
	@JsName("renderAds")
	fun renderAds() {
		val serverConfiguration = document.select<HTMLDivElement?>("#server-configuration")

		if (serverConfiguration != null) {
			debug("Fixing server configuration height due to AdSense being dum dum...")
			// Reset AdSense height fuckery

			val observer = MutationObserver { array, obs ->
				debug("Resetting server configuration height!")
				serverConfiguration.style.height = ""
			}

			observer.observe(serverConfiguration, MutationObserverInit(attributes = true, attributeFilter = arrayOf("style")))
		}

		val ads = document.selectAll<HTMLModElement>(".adsbygoogle")

		debug("There are ${ads.size} ads in the page...")

		ads.forEach {
			if (!it.hasAttribute("data-adsbygoogle-status")) {
				try {
					console.log(it)
					js("(adsbygoogle = window.adsbygoogle || []).push({});")
					debug("Yay!")
				} catch (e: Throwable) {
					console.log(e)
				}
			}
		}
	}
}