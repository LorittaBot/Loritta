package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.delay
import kotlinx.html.dom.append
import kotlinx.html.img
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.random.Random

object AdvertisementUtils : Logging {
	val isUserBlockingAds: Boolean
		get() = window.asDynamic().canRunAds == undefined

	fun checkIfUserIsBlockingAds() {
		if (isUserBlockingAds) {
			debug("User is blocking ads... :(")

			SpicyMorenitta.INSTANCE.launch {
				delay(5_000)

				val (adCount, rand) = replaceAllGoogleAdSenseAdsWithFakeAds()

				val replacedAds = document.selectAll<HTMLElement>(".lori$rand-help-plz-banner")
				debug("There are ${replacedAds.size} replaced ads")

				val hasAnyRemovedAds = replacedAds.any { it.style.display == "none" } || adCount > replacedAds.size

				if (hasAnyRemovedAds) {
					forceUserToDisableAdBlock()
				}
			}
		}
	}

	private fun replaceAllGoogleAdSenseAdsWithFakeAds(): AdSenseToFakeAdsResult {
		val rand = Random(0).nextLong()

		val ads = document.selectAll<HTMLElement>(".adsbygoogle")
		val adCount = ads.size
		debug("There are $adCount Google AdSense ads")

		ads.forEach {
			val parentElement = it.parentElement!!

			parentElement.append {
				img(src = "https://loritta.website/assets/img/lori_helpplz.png", classes = "lori$rand-help-plz-banner")
			}
		}

		return AdSenseToFakeAdsResult(
				adCount,
				rand
		)
	}

	private fun forceUserToDisableAdBlock() {
		debug("Looks like the user removed some ads... whoopsie, I tripped on the cord! >:)")
		val body = document.body!!

		body.clear()
		body.append {
			img(src = "https://i.imgur.com/MZWtUzB.png")
		}
	}

	private data class AdSenseToFakeAdsResult(
			val googleAdSenseAds: Int,
			val randomLong: Long
	)
}