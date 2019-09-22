package net.perfectdreams.loritta.watchdog.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

data class WatchdogConfig @JsonCreator constructor(
		val discordToken: String,
		val checkBots: List<BotConfig>
) {
	data class BotConfig @JsonCreator constructor(
			val name: String,
			val botId: Long,
			val channelId: Long,
			val clusterUrl: String,
			val websiteUrl: String,
			val packFiles: List<String>,
			val rollingDelayPerShard: Int,
			val clusters: List<LorittaClusterConfig>
	)
}