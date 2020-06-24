package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.lang.Exception

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	companion object {
	}

	fun migrateOldSimpleTextCommands() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		loritta.initPostgreSql()
		transaction(Databases.loritta) {
			CustomGuildCommands.selectAll().forEach {
				if (it[CustomGuildCommands.code].startsWith("// Loritta Auto Generated Custom Command - Do not edit!")) {
					try {
						val obj = JsonParser.parseString(it[CustomGuildCommands.code].lines()[1].removePrefix("// ")).obj

						transaction(Databases.loritta) {
							CustomGuildCommands.update({ CustomGuildCommands.id eq it[CustomGuildCommands.id] }) {
								it[CustomGuildCommands.codeType] = CustomCommandCodeType.SIMPLE_TEXT
								it[CustomGuildCommands.code] = obj["data"]["text"].string
							}
						}
					} catch (e: Exception) {
						println("Something went wrong when converting a command")
						e.printStackTrace()
					}
				}
			}
		}

		println("Finished!")
	}
}