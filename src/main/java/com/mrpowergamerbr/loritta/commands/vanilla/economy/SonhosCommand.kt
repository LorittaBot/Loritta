package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save

class SonhosCommand : AbstractCommand("sonhos", listOf("atm"), category = CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SONHOS_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var retrieveDreamsFromUser = context.getUserAt(0) ?: context.userHandle

		val lorittaProfile = if (retrieveDreamsFromUser == context.userHandle) {
			context.lorittaUser.profile
		} else {
			loritta.getLorittaProfileForUser(retrieveDreamsFromUser.id)
		}

		if (lorittaProfile.dreams.isNaN()) {
			lorittaProfile.dreams = 0.0
			loritta save lorittaProfile
			return
		}

		if (context.userHandle == retrieveDreamsFromUser) {
			if (context.config.economyConfig.isEnabled) {
				val localProfile = context.config.getUserData(lorittaProfile.userId)
				context.reply(
						true,
						LoriReply(
								locale["SONHOS_YouHave", lorittaProfile.dreams, if (lorittaProfile.dreams == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>",
								mentionUser = false
						),
						LoriReply(
								locale["SONHOS_YouHave", localProfile.money, if (localProfile.money == 1.0) { context.config.economyConfig.economyName } else { context.config.economyConfig.economyNamePlural }],
								"\uD83D\uDCB5",
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								locale["SONHOS_YouHave", lorittaProfile.dreams, if (lorittaProfile.dreams == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>"
						)
				)
			}
			logger.info("Usuário ${lorittaProfile.userId} possui ${lorittaProfile.dreams} sonhos!")
		} else {
			if (context.config.economyConfig.isEnabled) {
				val localProfile = context.config.getUserData(lorittaProfile.userId)
				context.reply(
						true,
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, lorittaProfile.dreams, if (lorittaProfile.dreams == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"<:loritta:331179879582269451>",
								mentionUser = false
						),
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, localProfile.money, if (lorittaProfile.dreams == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"\uD83D\uDCB5",
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, lorittaProfile.dreams, if (lorittaProfile.dreams == 1.0) { locale["ECONOMY_Name"] } else { locale["ECONOMY_NamePlural"] }],
								"\uD83D\uDCB5"
						)
				)
			}
			logger.info("Usuário ${retrieveDreamsFromUser.id} possui ${lorittaProfile.dreams} sonhos!")
		}
	}
}