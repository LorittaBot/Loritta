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
}