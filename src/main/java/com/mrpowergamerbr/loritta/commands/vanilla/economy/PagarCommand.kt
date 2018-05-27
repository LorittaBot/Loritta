package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class PagarCommand : AbstractCommand("pay", listOf("pagar"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PAY_Description"];
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.size >= 2) {
			val user = LorittaUtils.getUserFromContext(context, 0)
			val howMuch = context.rawArgs.getOrNull(1)?.toDoubleOrNull()

			if (user == null || context.userHandle == user) {
				context.reply(
						LoriReply(
								locale["REP_InvalidUser"],
								Constants.ERROR
						)
				)
				return
			}

			if (howMuch == null || howMuch.isNaN()) {
				context.reply(
						LoriReply(
								locale["INVALID_NUMBER", context.rawArgs[1]],
								Constants.ERROR
						)
				)
				return
			}

			if (0 >= howMuch || context.lorittaUser.profile.dreams.isNaN()) {
				context.reply(
						LoriReply(
								locale["INVALID_NUMBER", context.rawArgs[1]],
								Constants.ERROR
						)
				)
				return
			}

			if (howMuch > context.lorittaUser.profile.dreams) {
				context.reply(
						LoriReply(
								locale["PAY_InsufficientFunds"],
								Constants.ERROR
						)
				)
				return
			}

			// Hora de transferir!
			val receiverProfile = loritta.getLorittaProfileForUser(user.id)

			if (receiverProfile.dreams.isNaN()) {
				// receiverProfile.dreams = 0.0
				return
			}

			if (context.lorittaUser.profile.dreams.isNaN()) {
				// context.lorittaUser.profile.dreams = 0.0
				return
			}

			val beforeGiver = context.lorittaUser.profile.dreams
			val beforeReceiver = receiverProfile.dreams

			context.lorittaUser.profile.dreams -= howMuch
			receiverProfile.dreams += howMuch

			logger.info("${context.userHandle.id} (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos)")
			loritta save context.lorittaUser.profile
			loritta save receiverProfile

			context.reply(
					LoriReply(
							locale["PAY_TransactionComplete", user.asMention, howMuch],
							"\uD83D\uDCB8"
					)
			)
		} else {
			context.explain()
		}
	}
}