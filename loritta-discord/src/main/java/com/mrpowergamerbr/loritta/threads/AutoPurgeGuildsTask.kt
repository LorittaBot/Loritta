package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.Runnable
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.PurgeDiscordGuilds

class AutoPurgeGuildsTask : Runnable {
	private val logger = KotlinLogging.logger {}

	override fun run() {
		if (!FeatureFlags.AUTO_PURGE_GUILDS)
			return

		try {
			val guildsToBePurged = PurgeDiscordGuilds.getGuildsToBePurged(
					System.currentTimeMillis() - (Constants.ONE_MONTH_IN_MILLISECONDS * 12) // one year
			)

			logger.info { "${guildsToBePurged.size} guilds will be purged!" }

			guildsToBePurged.forEach { (guild, serverConfig) ->
				try {
					logger.info { "Leaving ${guild.name} (${guild.idLong}), owner ${guild.owner?.user?.name} (${guild.ownerIdLong}) due to guild inactivity... Member quantity: ${guild.members.size}" }

					guild.leave().complete()
				} catch (e: Exception) {
					logger.warn(e) { "Exception while leaving $guild" }
				}
			}

			logger.info { "${guildsToBePurged.size} guilds were successfully purged!" }
		} catch (e: Exception) {
			logger.warn(e) { "Exception while processing guild purges" }
		}
	}
}