package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.threads.LoteriaThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class LoterittaCommand : AbstractCommand("loteritta", listOf("loteria", "lottery"), CommandCategory.SOCIAL) {
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
				loritta.loteriaThread.save()
				loritta save lorittaProfile

				context.reply(
						LoriReply(
								"Você comprou ${quantity} ticket${if (quantity == 1) "" else "s"} por **${requiredCount} Sonhos**! Agora é só sentar e relaxar até o resultado da loteria sair!",
								"\uD83C\uDFAB"
						),
						LoriReply(
								"Querendo mais chances de ganhar? Que tal comprar outro ticket? \uD83D\uDE09 `${context.config.commandPrefix}loteria comprar [quantidade]`",
								mentionUser = false
						)
				)
			} else {
				context.reply(
						LoriReply(
								"Você precisa ter **+${requiredCount - lorittaProfile.dreams} Sonhos** para poder comprar ${quantity} ticket${if (quantity == 1) "" else "s"}!",
								Constants.ERROR
						)
				)
			}
			return
		}

		if (arg0 == "force") {
			loritta.loteriaThread.handleWin()
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
						"Prêmio atual: **${LoteriaThread.userIds.size * 250} Sonhos**",
						"<:twitt_starstruck:352216844603752450>",
						mentionUser = false
				),
				LoriReply(
						"Tickets comprados: **${LoteriaThread.userIds.size} Tickets**",
						"\uD83C\uDFAB",
						mentionUser = false
				),
				LoriReply(
						"Pessoas participando: **${LoteriaThread.userIds.distinctBy { it.first }.size} Pessoas**",
						"\uD83D\uDC65",
						mentionUser = false
				),
				LoriReply(
						"Último ganhador: `${nameAndDiscriminator.stripCodeMarks()} (${LoteriaThread.lastWinnerPrize} Sonhos)`",
						"\uD83D\uDE0E",
						mentionUser = false
				),
				LoriReply(
						"Resultado irá sair daqui a **${DateUtils.formatDateDiff(Calendar.getInstance(), cal, locale)}**!",
						prefix = "\uD83D\uDD52",
						mentionUser = false
				),
				LoriReply(
						"Compre um ticket por **250 Sonhos** usando `${context.config.commandPrefix}loteria comprar`!",
						prefix = "\uD83D\uDCB5",
						mentionUser = false
				)
		)
	}
}