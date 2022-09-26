package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.delay
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import kotlinx.dom.hasClass

fun TingleModal.trackOverflowChanges(m: SpicyMorenitta) {
	AdvertisementUtils.debug("Tracking $this overflow changes...")

	m.launch {
		while (visibleModal.hasClass("tingle-modal--visible")) {
			this@trackOverflowChanges.checkOverflow()
			delay(100)
		}

		AdvertisementUtils.debug("Modal $this was closed, we will stop tracking overflow changes...")
	}
}