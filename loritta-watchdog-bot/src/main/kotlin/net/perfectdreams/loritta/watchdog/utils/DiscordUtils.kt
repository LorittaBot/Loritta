package net.perfectdreams.loritta.watchdog.utils

import net.perfectdreams.loritta.watchdog.utils.config.WatchdogConfig

object DiscordUtils {
	fun getUrlForLorittaClusterId(cluster: WatchdogConfig.BotConfig, id: Long): String {
		val website = cluster.websiteUrl

		if (id == 1L)
			return website.substring(website.indexOf("//") + 2).removeSuffix("/")

		return cluster.clusterUrl.format(id)
	}
}