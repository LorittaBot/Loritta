package net.perfectdreams.loritta.watchdog.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

data class WatchdogConfig @JsonCreator constructor(
		val discordToken: String,
		val checkBots: List<BotConfig>
) {
	data class BotConfig @JsonCreator constructor(
			val delay: Long,
			val startAfter: Long,
			val botId: Long,
			val channelId: Long,
			val useCommand: String,
			val timeout: Long,
			val warnAfter: Long
	)
}