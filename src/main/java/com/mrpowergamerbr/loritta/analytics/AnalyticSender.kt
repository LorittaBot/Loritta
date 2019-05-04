package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.Loritta

/**
 * Sends analytics to analytic processing websites (also known as: Discord Bots Lists)
 */
class AnalyticSender : Runnable {
	override fun run() {
		if (Loritta.config.discordBots.enabled)
			LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOTS)
		if (Loritta.config.discordBotList.enabled)
			LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOT_LIST)
	}
}