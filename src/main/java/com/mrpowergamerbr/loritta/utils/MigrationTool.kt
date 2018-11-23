package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class MigrationTool(val config: LorittaConfig) {
	fun migrateWarns() {
		val loritta = Loritta(config)
		loritta.initMongo()
		loritta.initPostgreSql()

		println("Migrando avisos...")

		val servers = loritta.serversColl.find()

		servers.iterator().use {
			while (it.hasNext()) {
				val next = it.next()

				transaction(Databases.loritta) {
					next.guildUserData.filter { it.warns.isNotEmpty() }.forEach { userData ->
						userData.warns.forEach {
							Warn.new {
								this.guildId = next.guildId.toLong()
								this.userId = userData.userId.toLong()
								this.receivedAt = it.time
								this.punishedById = it.punishedBy.toLong()
								this.content = it.reason
							}
						}
					}
				}
			}
		}

		println("Avisos migrados com sucesso!")
	}

	fun migrateLocalProfiles() {
		val loritta = Loritta(config)
		loritta.initMongo()
		loritta.initPostgreSql()

		println("Migrando perfis locais...")

		val servers = loritta.serversColl.find()

		servers.iterator().use {
			while (it.hasNext()) {
				val next = it.next()

				transaction(Databases.loritta) {
					next.guildUserData.forEach { userData ->
						GuildProfile.new {
							this.guildId = next.guildId.toLong()
							this.userId = userData.userId.toLong()
							this.money = BigDecimal.ZERO
							this.quickPunishment = userData.quickPunishment
							this.xp = userData.xp
						}
					}
				}
			}
		}

		println("Perfis locais migrados com sucesso!")
	}
}