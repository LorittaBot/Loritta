package com.mrpowergamerbr.loritta.analytics

/**
 * Sends analytics to analytic processing websites (also known as: Discord Bots Lists)
 */
class AnalyticSender : Runnable {
	override fun run() {
		LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOTS)
		LorittaAnalytics.send(AnalyticProcessorService.DISCORD_BOT_LIST)
	}
}