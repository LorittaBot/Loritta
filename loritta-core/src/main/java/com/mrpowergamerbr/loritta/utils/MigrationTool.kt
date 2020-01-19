package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import org.jetbrains.exposed.sql.transactions.transaction

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	fun migrateGeneralConfig() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		loritta.initMongo()
		loritta.initPostgreSql()

		println("Migrando configs gerais...")
		val servers = loritta.serversColl.find()

		var all = 0
		var success = 0

		servers.iterator().use {
			transaction(Databases.loritta) {
				while (it.hasNext()) {
					all++
					val next = it.next()

					// Se a config não existe, apenas ignore!
					val serverConfig = ServerConfig.findById(next.guildId.toLong()) ?: continue
					serverConfig.commandPrefix = next.commandPrefix
					serverConfig.localeId = next.localeId
					serverConfig.warnOnUnknownCommand = next.warnOnUnknownCommand
					serverConfig.warnOnMissingPermission = next.warnOnMissingPermission
					serverConfig.warnIfBlacklisted = next.warnIfBlacklisted
					serverConfig.deleteMessageAfterCommand = next.deleteMessageAfterCommand

					if (!serverConfig.warnIfBlacklisted)
						serverConfig.blacklistedWarning = next.blacklistWarning

					serverConfig.blacklistedChannels = next.blacklistedChannels.mapNotNull { it.toLongOrNull() }
							.toTypedArray()

					success++
				}
			}
		}

		println("Sucesso! $success de $all configurações foram migradas ;3")
	}
}