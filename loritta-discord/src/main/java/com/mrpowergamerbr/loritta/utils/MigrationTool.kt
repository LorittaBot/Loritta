package com.mrpowergamerbr.loritta.utils

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.tables.TrackedTwitchAccounts
import net.perfectdreams.loritta.tables.TrackedYouTubeAccounts
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	companion object {
		fun migrateGeneralConfig(serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
			transaction(Databases.loritta) {
				serverConfig.commandPrefix = legacyServerConfig.commandPrefix
				serverConfig.localeId = legacyServerConfig.localeId
				serverConfig.warnOnUnknownCommand = legacyServerConfig.warnOnUnknownCommand
				serverConfig.warnOnMissingPermission = legacyServerConfig.warnOnMissingPermission
				serverConfig.warnIfBlacklisted = legacyServerConfig.warnIfBlacklisted
				serverConfig.deleteMessageAfterCommand = legacyServerConfig.deleteMessageAfterCommand

				if (serverConfig.warnIfBlacklisted)
					serverConfig.blacklistedWarning = legacyServerConfig.blacklistWarning

				serverConfig.blacklistedChannels = legacyServerConfig.blacklistedChannels.mapNotNull { it.toLongOrNull() }
						.toTypedArray()
			}
		}

		fun fixWarnIfBlacklistedMessage(serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
			transaction(Databases.loritta) {
				if (serverConfig.warnIfBlacklisted && serverConfig.blacklistedWarning == null)
					serverConfig.blacklistedWarning = legacyServerConfig.blacklistWarning
			}
		}
	}

	fun migrateYouTubeChannels() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		loritta.initMongo()
		loritta.initPostgreSql()

		println("Migrando canais do YouTube...")
		val servers = com.mrpowergamerbr.loritta.utils.loritta.serversColl.find(
				Filters.gt("youTubeConfig.channels", listOf<Any>())
		)

		var totalChannelsMigrated = 0

		servers.iterator().use {
			transaction(Databases.loritta) {
				while (it.hasNext()) {
					var perServerChannel = 0
					val next = it.next()

					for (channel in next.youTubeConfig.channels) {
						if (perServerChannel == 5) {
							println("Server ${next.guildId} has more than 5 channels, ignoring...")
							break
						}

						val channelId = channel.channelId
						val repostToChannelId = channel.repostToChannelId?.toLongOrNull()
						val message = channel.videoSentMessage

						if (channelId != null && repostToChannelId != null && message != null) {
							TrackedYouTubeAccounts.insert {
								it[guildId] = next.guildId.toLong()
								it[youTubeChannelId] = channelId
								it[TrackedYouTubeAccounts.channelId] = repostToChannelId
								it[TrackedYouTubeAccounts.message] = message
							}
							perServerChannel++
							totalChannelsMigrated++
						}
					}
				}
			}
		}

		println("Sucesso! $totalChannelsMigrated canais do YouTube foram migrados ;3")
	}

	fun migrateTwitchChannels() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		loritta.initMongo()
		loritta.initPostgreSql()

		println("Migrando canais da Twitch...")
		val servers = com.mrpowergamerbr.loritta.utils.loritta.serversColl.find(
				Filters.gt("livestreamConfig.channels", listOf<Any>())
		)

		val userLogins = mutableSetOf<String>()

		servers.iterator().use {
			while (it.hasNext()) {
				val server = it.next()
				// val guild = lorittaShards.getGuildById(server.guildId) ?: continue
				val livestreamConfig = server.livestreamConfig

				for (channel in livestreamConfig.channels) {
					if (channel.channelUrl == null)
						continue
					if (!channel.channelUrl!!.startsWith("http"))
						continue
					if (!channel.channelUrl!!.contains("twitch"))
						continue

					/* val textChannel = guild.getTextChannelByNullableId(channel.repostToChannelId) ?: continue

					if (!textChannel.canTalk())
						continue */

					val userLogin = channel.channelUrl!!.split("/").last()
					if (userLogin.isBlank())
						continue

					// Algumas verificações antes de adicionar
					if (!Constants.TWITCH_USERNAME_PATTERN.matcher(userLogin).matches())
						continue

					userLogins.add(userLogin)
				}
				// list.add(server)
			}
		}

		// Transformar todos os nossos user logins em user IDs, para que seja usado depois
		val streamerInfos = runBlocking { com.mrpowergamerbr.loritta.utils.loritta.twitch.getUserLogins(userLogins.toMutableList()) }

		var totalChannelsMigrated = 0

		servers.iterator().use {
			transaction(Databases.loritta) {
				while (it.hasNext()) {
					var perServerChannel = 0
					val next = it.next()

					for (channel in next.livestreamConfig.channels) {
						if (perServerChannel == 5) {
							println("Server ${next.guildId} has more than 5 channels, ignoring...")
							break
						}

						if (channel.channelUrl == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						/* val textChannel = guild.getTextChannelByNullableId(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue */

						val userLogin = channel.channelUrl!!.split("/").last()
						if (userLogin.isBlank())
							continue

						// Algumas verificações antes de adicionar
						if (!Constants.TWITCH_USERNAME_PATTERN.matcher(userLogin).matches())
							continue

						val userId = streamerInfos[userLogin]?.id ?: continue

						val repostToChannelId = channel.repostToChannelId?.toLongOrNull()
						val message = channel.videoSentMessage

						if (repostToChannelId != null && message != null) {
							TrackedTwitchAccounts.insert {
								it[guildId] = next.guildId.toLong()
								it[twitchUserId] = userId.toLong()
								it[TrackedTwitchAccounts.channelId] = repostToChannelId
								it[TrackedTwitchAccounts.message] = message
							}
							perServerChannel++
							totalChannelsMigrated++
						}
					}
				}
			}
		}

		println("Sucesso! $totalChannelsMigrated canais da Twitch foram migrados ;3")
	}
}