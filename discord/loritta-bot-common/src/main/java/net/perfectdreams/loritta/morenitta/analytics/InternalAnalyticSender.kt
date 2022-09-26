package net.perfectdreams.loritta.morenitta.analytics

import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import mu.KotlinLogging

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			DebugLog.showExtendedInfo()
		} catch (e: Exception) {
			logger.error("Erro ao mostrar analytics", e)
		}
	}
}