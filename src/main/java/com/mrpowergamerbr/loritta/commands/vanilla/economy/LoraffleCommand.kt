package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.threads.RaffleThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class LoraffleCommand : AbstractCommand("loraffle", listOf("rifa", "raffle", "lorifa"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["RAFFLE_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "clear" && context.userHandle.id == Loritta.config.ownerId) {
			context.reply(
					LoriReply(
							"Limpando ${RaffleThread.userIds.size}..."
					)
			)
			RaffleThread.userIds.clear()
			context.reply(
					LoriReply(
							"Limpo! ${RaffleThread.userIds.size}"
					)
			)
			return
		}

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toIntOrNull() ?: 1, 1)

			val requiredCount = quantity.toLong() * 250
			RaffleThread.logger.info("${context.userHandle.id} irá comprar $quantity tickets por ${requiredCount}!")

			synchronized(this) {
				val lorittaProfile = loritta.getLorittaProfileForUser(context.userHandle.id)

				if (lorittaProfile.dreams >= requiredCount) {
					lorittaProfile.dreams -= requiredCount
					loritta save lorittaProfile

					for (i in 0 until quantity) {
						RaffleThread.userIds.add(Pair(context.userHandle.id, context.config.localeId))
					}

					RaffleThread.logger.info("${context.userHandle.id} comprou $quantity tickets por ${requiredCount}! (Antes ele possuia ${lorittaProfile.dreams + requiredCount}) sonhos!")

					loritta.raffleThread.save()

					context.reply(
							LoriReply(
									context.locale["RAFFLE_YouBoughtAnTicket", quantity, if (quantity == 1) "" else "s", requiredCount],
									"\uD83C\uDFAB"
							),
							LoriReply(
									context.locale["RAFFLE_WantMoreChances", context.config.commandPrefix],
									mentionUser = false
							)
					)
				} else {
					context.reply(
							LoriReply(
									context.locale["RAFFLE_NotEnoughMoney", requiredCount - lorittaProfile.dreams, quantity, if (quantity == 1) "" else "s"],
									Constants.ERROR
							)
					)
				}
			}
			return
		}

		val cal = Calendar.getInstance()
		cal.timeInMillis = RaffleThread.started + 3600000

		val lastWinner = if (RaffleThread.lastWinnerId != null) {
			lorittaShards.getUserById(RaffleThread.lastWinnerId)
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
						"**Lorifa**",
						"<:loritta:331179879582269451>"
				),
				LoriReply(
						context.locale["RAFFLE_CurrentPrize", (RaffleThread.userIds.size * 250).toString()],
						"<:twitt_starstruck:352216844603752450>",
						mentionUser = false
				),
				LoriReply(
						context.locale["RAFFLE_BoughtTickets", RaffleThread.userIds.size],
						"\uD83C\uDFAB",
						mentionUser = false
				),
				LoriReply(
						context.locale["RAFFLE_UsersParticipating", RaffleThread.userIds.distinctBy { it.first }.size],
						"\uD83D\uDC65",
						mentionUser = false
				),
				LoriReply(
						context.locale["RAFFLE_LastWinner", nameAndDiscriminator.stripCodeMarks(), RaffleThread.lastWinnerPrize],
						"\uD83D\uDE0E",
						mentionUser = false
				),
				LoriReply(
						context.locale["RAFFLE_ResultsIn", DateUtils.formatDateDiff(Calendar.getInstance(), cal, locale)],
						prefix = "\uD83D\uDD52",
						mentionUser = false
				),
				LoriReply(
						context.locale["RAFFLE_BuyAnTicketFor", context.config.commandPrefix],
						prefix = "\uD83D\uDCB5",
						mentionUser = false
				)
		)
	}
}