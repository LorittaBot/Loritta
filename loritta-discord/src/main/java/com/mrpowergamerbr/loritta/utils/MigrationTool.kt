package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.tables.BannedUsers
import org.jetbrains.exposed.sql.insert

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	companion object {
	}

	fun migrateBannedUsers() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)
		loritta.initPostgreSql()
		loritta.transaction {
			val profiles = Profile.find { Profiles.isBanned eq true }

			for (profile in profiles) {
				BannedUsers.insert {
					it[userId] = profile.userId
					it[bannedAt] = System.currentTimeMillis()
					it[bannedBy] = null
					it[valid] = true
					it[expiresAt] = null
					it[BannedUsers.reason] = profile.bannedReason ?: "¯\\_(ツ)_/¯"
				}
			}
		}
		println("Done!")
	}
}