package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.threads.LoteriaThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class LoterittaCommand : AbstractCommand("loteritta", listOf("loteria", "lottery"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LOTERIA_Description"];
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toIntOrNull() ?: 1, 1)

			val lorittaProfile = context.lorittaUser.profile
			val requiredCount = quantity * 250
			if (lorittaProfile.dreams >= requiredCount) {
				lorittaProfile.dreams -= requiredCount
				for (i in 0 until quantity) {
					LoteriaThread.userIds.add(Pair(context.userHandle.id, context.config.localeId))
				}
				LoteriaThread.logger.info("${context.userHandle.id} comprou $quantity tickets por ${requiredCount}! (Antes ele possuia ${lorittaProfile.dreams + requiredCount}) sonhos!")

				loritta save lorittaProfile
				loritta.loteriaThread.save()

				context.reply(
						LoriReply(
								context.locale["LOTERIA_YouBoughtAnTicket", quantity, if (quantity == 1) "" else "s", requiredCount],
								"\uD83C\uDFAB"
						),
						LoriReply(
								context.locale["LOTERIA_WantMoreChances", context.config.commandPrefix],
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								context.locale["LOTERIA_NotEnoughMoney", requiredCount - lorittaProfile.dreams, quantity, if (quantity == 1) "" else "s"],
								Constants.ERROR
						)
				)
			}
			return
		}

		val cal = Calendar.getInstance()
		cal.timeInMillis = LoteriaThread.started + 3600000

		val lastWinner = if (LoteriaThread.lastWinnerId != null) {
			lorittaShards.getUserById(LoteriaThread.lastWinnerId)
		} else {
			null
		}

		var nameAndDiscriminator = if (lastWinner != null) {
			lastWinner.name + "#" + lastWinner.discriminator
		} else {
			"\uD83E\uDD37"
		}

		context.reply(
				LoriReply(
						"**Loteritta**",
						"<:loritta:331179879582269451>"
				),
				LoriReply(
						context.locale["LOTERIA_CurrentPrize", (LoteriaThread.userIds.size * 250).toString()],
						"<:twitt_starstruck:352216844603752450>",
						mentionUser = false
				),
				LoriReply(
						context.locale["LOTERIA_BoughtTickets", LoteriaThread.userIds.size],
						"\uD83C\uDFAB",
						mentionUser = false
				),
				LoriReply(
						context.locale["LOTERIA_UsersParticipating", LoteriaThread.userIds.distinctBy { it.first }.size],
						"\uD83D\uDC65",
						mentionUser = false
				),
				LoriReply(
						context.locale["LOTERIA_LastWinner", nameAndDiscriminator.stripCodeMarks(), LoteriaThread.lastWinnerPrize],
						"\uD83D\uDE0E",
						mentionUser = false
				),
				LoriReply(
						context.locale["LOTERIA_ResultsIn", DateUtils.formatDateDiff(Calendar.getInstance(), cal, locale)],
						prefix = "\uD83D\uDD52",
						mentionUser = false
				),
				LoriReply(
						context.locale["LOTERIA_BuyAnTicketFor", context.config.commandPrefix],
						prefix = "\uD83D\uDCB5",
						mentionUser = false
				)
		)
	}
}