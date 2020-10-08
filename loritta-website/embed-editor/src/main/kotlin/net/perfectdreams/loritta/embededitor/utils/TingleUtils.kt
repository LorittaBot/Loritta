package net.perfectdreams.loritta.embededitor.utils

import kotlinx.coroutines.delay
import net.perfectdreams.loritta.embededitor.select
import org.w3c.dom.Element
import kotlin.browser.document
// import net.perfectdreams.spicymorenitta.SpicyMorenitta
import kotlin.dom.hasClass

/* fun TingleModal.trackOverflowChanges(m: SpicyMorenitta) {
	AdvertisementUtils.debug("Tracking $this overflow changes...")

	m.launch {
		while (visibleModal.hasClass("tingle-modal--visible")) {
			this@trackOverflowChanges.checkOverflow()
			delay(100)
		}

		AdvertisementUtils.debug("Modal $this was closed, we will stop tracking overflow changes...")
	}
} */

val visibleModal: Element
    get() = document.select(".tingle-modal--visible")