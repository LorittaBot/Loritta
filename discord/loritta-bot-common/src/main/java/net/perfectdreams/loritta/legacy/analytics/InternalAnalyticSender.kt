package net.perfectdreams.loritta.legacy.analytics

import net.perfectdreams.loritta.legacy.utils.debug.DebugLog
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