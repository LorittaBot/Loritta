package net.perfectdreams.loritta.morenitta.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot

class SponsorsSyncTask(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			loritta.sponsors = SponsorManager.retrieveActiveSponsorsFromDatabase(loritta)
		} catch (e: Exception) {
			logger.error("Error while retrieving active sponsors!", e)
		}
	}
}