package net.perfectdreams.loritta.legacy.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.legacy.utils.SponsorManager

class SponsorsSyncTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			loritta.sponsors = SponsorManager.retrieveActiveSponsorsFromDatabase()
		} catch (e: Exception) {
			logger.error("Error while retrieving active sponsors!", e)
		}
	}
}