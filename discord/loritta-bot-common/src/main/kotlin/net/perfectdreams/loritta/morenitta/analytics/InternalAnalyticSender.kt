package net.perfectdreams.loritta.morenitta.analytics

import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender(val loritta: LorittaBot) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        try {
            DebugLog.showExtendedInfo(loritta)
        } catch (e: Exception) {
            logger.error("Erro ao mostrar analytics", e)
        }
    }
}