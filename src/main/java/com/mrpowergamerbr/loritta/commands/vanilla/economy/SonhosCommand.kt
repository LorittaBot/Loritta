package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

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
			context.reply(
					LoriReply(
							locale["SONHOS_YouHave", lorittaProfile.dreams],
							"\uD83D\uDCB5"
					)
			)
			logger.info("Usuário ${lorittaProfile.userId} possui ${lorittaProfile.dreams} sonhos!")
		} else {
			context.reply(
					LoriReply(
							locale["SONHOS_UserHas", retrieveDreamsFromUser.asMention, lorittaProfile.dreams],
							"\uD83D\uDCB5"
					)
			)
			logger.info("Usuário ${retrieveDreamsFromUser.id} possui ${lorittaProfile.dreams} sonhos!")
		}
	}
}