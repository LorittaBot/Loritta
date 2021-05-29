package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.utils.debug.DebugLog
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
			DebugLog.dumpCoroutinesToFile()
		} catch (e: Exception) {
			logger.error("Erro ao mostrar analytics", e)
		}
	}
}