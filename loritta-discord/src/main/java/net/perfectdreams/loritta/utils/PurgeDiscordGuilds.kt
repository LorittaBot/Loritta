package net.perfectdreams.loritta.utils

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Guild

object PurgeDiscordGuilds {
	/**
	 * Get guilds that can be purged due to bot inactivity
	 *
	 * @param lastCommandReceivedBefore how many ms should have been passed for a guild to be considered "inactive"
	 *
	 * @return a list containing all matched guilds with their server configs
	 */
	fun getGuildsToBePurged(lastCommandReceivedBefore: Long): List<GuildAndServerConfig> {
		val stuff = loritta.serversColl.find(
				Filters.lte("lastCommandReceivedAt", lastCommandReceivedBefore)
		).toMutableList().filter {
			!it.joinLeaveConfig.isEnabled &&
					it.livestreamConfig.channels.isEmpty() &&
					!it.starboardConfig.isEnabled &&
					!it.eventLogConfig.isEnabled &&
					!it.autoroleConfig.isEnabled &&
					!it.inviteBlockerConfig.isEnabled
		}

		return stuff.mapNotNull {
			val guild = lorittaShards.getGuildById(it.guildId)

			if (guild != null)
				GuildAndServerConfig(guild, it)
			else null
		}.filter {
			// Verificar se ela entrou a mais do que a última vez que executaram comandos, as vezes "0" pode significar "eu acabei de entrar e nunca usaram comandos meus!"
			lastCommandReceivedBefore > it.guild.selfMember.timeJoined.toInstant().toEpochMilli()
		}
	}

	data class GuildAndServerConfig(
			val guild: Guild,
			val serverConfig: MongoServerConfig
	)
}