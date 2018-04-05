package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.threads.LoteriaThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class PagarCommand : AbstractCommand("pay", listOf("pagar"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PAY_Description"];
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.size >= 2) {
			val user = LorittaUtils.getUserFromContext(context, 0)
			val howMuch = context.rawArgs.getOrNull(1)?.toDoubleOrNull()

			if (user == null) {
				context.reply(
						LoriReply(
								locale["REP_InvalidUser"],
								Constants.ERROR
						)
				)
				return
			}

			if (howMuch == null) {
				context.reply(
						LoriReply(
								locale["INVALID_NUMBER", context.rawArgs[1]],
								Constants.ERROR
						)
				)
				return
			}

			if (0 >= howMuch) {
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
			context.lorittaUser.profile.dreams -= howMuch
			receiverProfile.dreams += howMuch

			logger.info("${context.userHandle.id} transferiu ${howMuch} sonhos para ${receiverProfile.userId}")
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