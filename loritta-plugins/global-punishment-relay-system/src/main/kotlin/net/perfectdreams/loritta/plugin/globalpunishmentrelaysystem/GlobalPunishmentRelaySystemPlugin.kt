package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.LGPRSCheckGuildCommand
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.LGPRSCheckPunishmentCommand
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.LGPRSCheckUserCommand
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.LGPRSReportCommand
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.MessageProofs
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.ScreenshotProofs
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.UserReports
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GlobalPunishmentRelaySystemPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onEnable() {
		super.onEnable()

		loritta as Loritta

		if (loritta.config.loritta.environment == EnvironmentType.CANARY) {
			registerCommands(
					LGPRSReportCommand.command(loritta, this),
					LGPRSCheckUserCommand.command(loritta, this),
					LGPRSCheckGuildCommand.command(loritta, this),
					LGPRSCheckPunishmentCommand.command(loritta, this)
			)

			transaction(Databases.loritta) {
				SchemaUtils.createMissingTablesAndColumns(
						UserReports,
						MessageProofs,
						ScreenshotProofs
				)
			}
		}
	}

	override fun onDisable() {
		super.onDisable()
	}

	fun calculateGuildReputationScore(guildId: Long): Int {
		val allUserReports = transaction(Databases.loritta) {
			UserReports.select {
				UserReports.guildId eq guildId
			}.toList()
		}

		val validReports = allUserReports.filter { it[UserReports.approved] }
		val revokedReports = allUserReports.filter { it[UserReports.revoked] }

		var startBase = 50

		validReports.forEach {
			startBase++
		}

		startBase = Math.min(100, startBase)

		revokedReports.forEach {
			startBase -= 5
		}

		startBase = Math.max(0, startBase)

		return startBase
	}

	fun calculateUserReputationScore(userId: Long): Int {
		val allUserReports = transaction(Databases.loritta) {
			UserReports.select {
				UserReports.userId eq userId
			}.toList()
		}

		val validReports = allUserReports.filter { it[UserReports.approved] }
		val revokedReports = allUserReports.filter { it[UserReports.revoked] }

		var startBase = 50

		validReports.forEach {
			startBase++
		}

		startBase = Math.min(100, startBase)

		revokedReports.forEach {
			startBase -= 5
		}

		startBase = Math.max(0, startBase)

		return startBase
	}
}