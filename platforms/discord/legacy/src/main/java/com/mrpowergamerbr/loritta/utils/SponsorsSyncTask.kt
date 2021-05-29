package com.mrpowergamerbr.loritta.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.utils.SponsorManager

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