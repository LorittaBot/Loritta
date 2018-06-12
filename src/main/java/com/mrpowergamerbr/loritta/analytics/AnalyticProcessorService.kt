package com.mrpowergamerbr.loritta.analytics

/**
 * All services that processes our bot analytics
 */
enum class AnalyticProcessorService(val endpoint: String) {
	DISCORD_BOTS("https://bots.discord.pw/api/bots/%s/stats"),
	DISCORD_BOT_LIST("https://discordbots.org/api/bots/%s/stats"),
}