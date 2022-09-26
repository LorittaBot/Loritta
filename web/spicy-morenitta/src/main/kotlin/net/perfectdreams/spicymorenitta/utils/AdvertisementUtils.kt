package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.delay
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import net.perfectdreams.spicymorenitta.CookiesUtils
import net.perfectdreams.spicymorenitta.CookiesUtils.createCookie
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.locale
import org.w3c.dom.HTMLElement
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.clear
import kotlin.random.Random

object AdvertisementUtils : Logging {
	private const val DO_NOT_SHOW_ADBLOCK_POPUP_COOKIE = "doNotShowAdblockPopup"

	val isUserBlockingAds: Boolean
		get() = window.asDynamic().canRunAds == undefined

	fun checkIfUserIsBlockingAds() {
		if (isUserBlockingAds) {
			debug("Reading adblock popup cookie...")
			val cookie = CookiesUtils.readCookie(DO_NOT_SHOW_ADBLOCK_POPUP_COOKIE)

			debug("User is blocking ads... :( Did the user ask us to not bother him? $cookie")

			if (cookie != "true")
				openDisableAdblockModal()

			SpicyMorenitta.INSTANCE.launch {
				delay(5_000)

				debug("Replacing all Google AdSense ads with fake ads...")

				val (adCount, rand) = replaceAllGoogleAdSenseAdsWithFakeAds()

				val replacedAds = document.selectAll<HTMLElement>(".lori-$rand-help-plz-banner")
				debug("There are ${replacedAds.size} replaced ads")

				val hasAnyRemovedAds = replacedAds.any { it.style.display == "none" } || adCount > replacedAds.size

				if (hasAnyRemovedAds)
					forceUserToDisableAdBlock()
			}
		} else {
			debug("User is not blocking ads, yay!")
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
				img(src = "https://loritta.website/assets/img/lori_helpplz.png", classes = "lori-$rand-help-plz-banner")
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

	fun openDisableAdblockModal() {
		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.addFooterBtn("<i class=\"far fa-thumbs-up\"></i> ${locale["website.antiAdblock.disabledAdblock"]}", "button-discord button-discord-info pure-button button-discord-modal") {
			window.location.reload()
		}

		modal.addFooterBtn(locale["website.antiAdblock.maybeLater"], "button-discord button-discord-info pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
			createCookie(DO_NOT_SHOW_ADBLOCK_POPUP_COOKIE, "true", 3)
		}

		modal.setContent(createHTML().div {
			div {
				style = "text-align: center;"
				img(src = "https://loritta.website/assets/img/fanarts/l4.png") {
					width = "250"
				}

				locale.getList("website.antiAdblock.pleaseDisable").forEach {
					p {
						+ it
					}
				}
			}
		})

		modal.open()

		modal.trackOverflowChanges(SpicyMorenitta.INSTANCE)
	}
}