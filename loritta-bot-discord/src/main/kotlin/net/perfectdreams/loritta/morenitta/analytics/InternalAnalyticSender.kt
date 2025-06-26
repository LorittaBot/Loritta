package net.perfectdreams.loritta.morenitta.analytics

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override fun run() {
		try {
			DebugLog.showExtendedInfo(loritta)
		} catch (e: Exception) {
			logger.error(e) { "Erro ao mostrar analytics" }
		}
	}
}