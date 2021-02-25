package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * Sends analytics to analytic processing websites (also known as: Discord Bots Lists)
 */
class AnalyticSender : Runnable {
	private val logger = KotlinLogging.logger {}

	override fun run() {
		try {
			if (!loritta.isMaster) // Apenas o cluster principal sincroniza os servidores
				return

			val guildCount = runBlocking { lorittaShards.queryGuildCountOrThrowExceptionIfAnyClusterIsNotReady() }

			if (loritta.discordConfig.discordBots.enabled)
				LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOTS, guildCount)
			if (loritta.discordConfig.discordBotList.enabled)
				LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOT_LIST, guildCount)
		} catch (e: Exception) {
			logger.error(e) { "Something went wrong while trying to update analytics" }
		}
	}
}