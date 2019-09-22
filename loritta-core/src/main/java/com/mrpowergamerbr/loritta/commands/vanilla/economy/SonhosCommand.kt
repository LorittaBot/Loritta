package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class SonhosCommand : AbstractCommand("sonhos", listOf("atm"), category = CommandCategory.ECONOMY) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SONHOS_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val retrieveDreamsFromUser = context.getUserAt(0) ?: context.userHandle

		val lorittaProfile = if (retrieveDreamsFromUser == context.userHandle) {
			context.lorittaUser.profile
		} else {
			loritta.getOrCreateLorittaProfile(retrieveDreamsFromUser.id)
		}

		if (lorittaProfile.money.isNaN()) {
			transaction(Databases.loritta) {
				lorittaProfile.money = 0.0
			}
			return
		}


		val economyConfig = transaction(Databases.loritta) {
			loritta.getOrCreateServerConfig(context.guild.idLong).economyConfig
		}

		val localEconomyEnabled = economyConfig?.enabled == true

		if (context.userHandle == retrieveDreamsFromUser) {
			if (localEconomyEnabled && economyConfig != null) { // Sistema de ecnomia local está ativado!
				val localProfile = context.config.getUserData(lorittaProfile.userId)
				context.reply(
						true,
						LoriReply(
								locale["SONHOS_YouHave", lorittaProfile.money, if (lorittaProfile.money == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>",
								mentionUser = false
						),
						LoriReply(
								locale["SONHOS_YouHave", localProfile.money, if (localProfile.money == BigDecimal.ONE) { economyConfig.economyName } else { economyConfig.economyNamePlural }],
								"\uD83D\uDCB5",
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								locale["SONHOS_YouHave", lorittaProfile.money, if (lorittaProfile.money == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>"
						)
				)
			}
			logger.info("Usuário ${lorittaProfile.userId} possui ${lorittaProfile.money} sonhos!")
		} else {
			if (localEconomyEnabled && economyConfig != null) {
				val localProfile = context.config.getUserData(lorittaProfile.userId)
				context.reply(
						true,
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, lorittaProfile.money, if (lorittaProfile.money == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>",
								mentionUser = false
						),
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, localProfile.money, if (lorittaProfile.money == 1.0) { economyConfig.economyName } else { economyConfig.economyNamePlural }],
								"\uD83D\uDCB5",
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, lorittaProfile.money, if (lorittaProfile.money == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"\uD83D\uDCB5"
						)
				)
			}
			logger.info("Usuário ${retrieveDreamsFromUser.id} possui ${lorittaProfile.money} sonhos!")
		}
	}
}