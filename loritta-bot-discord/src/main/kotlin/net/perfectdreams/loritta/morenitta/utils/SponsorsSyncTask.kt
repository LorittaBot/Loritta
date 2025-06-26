package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot

class SponsorsSyncTask(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override fun run() {
		try {
			loritta.sponsors = SponsorManager.retrieveActiveSponsorsFromDatabase(loritta)
		} catch (e: Exception) {
			logger.error(e) { "Error while retrieving active sponsors!" }
		}
	}
}