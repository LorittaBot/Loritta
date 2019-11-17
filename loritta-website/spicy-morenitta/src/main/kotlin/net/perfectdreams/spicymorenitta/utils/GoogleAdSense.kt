package net.perfectdreams.spicymorenitta.utils

import org.w3c.dom.HTMLModElement
import kotlin.browser.document

object GoogleAdSense {
	@JsName("renderAds")
	fun renderAds() {
		val ads = document.selectAll<HTMLModElement>(".adsbygoogle")

		println("There are ${ads.size} ads in the page...")

		ads.forEach {
			if (!it.hasAttribute("data-adsbygoogle-status")) {
				println("Rendering ads... owo")
				console.log(it)
				js("(adsbygoogle = window.adsbygoogle || []).push({});")
			}
		}
	}
}