package net.perfectdreams.loritta.watchdog.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import net.perfectdreams.loritta.watchdog.utils.DiscordUtils

class LorittaClusterConfig @JsonCreator constructor(
		val id: Long,
		val name: String,
		val minShard: Long,
		val maxShard: Long,
		val folder: String,
		val ipPortForward: String,
		val targetIp: String,
		val key: String,
		val password: String,
		val apiKey: String
) {
	fun getUrl(config: WatchdogConfig.BotConfig) = DiscordUtils.getUrlForLorittaClusterId(config, id)
}