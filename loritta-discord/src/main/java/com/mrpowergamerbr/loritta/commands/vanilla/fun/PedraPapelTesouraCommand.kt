package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Jankenpon
import com.mrpowergamerbr.loritta.utils.Jankenpon.JankenponStatus
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory

class PedraPapelTesouraCommand : AbstractCommand("jankenpon", listOf("pedrapapeltesoura", "ppt"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.fun.rockpaperscissors.description"]
	}

	override fun getUsage(): String {
		return "sua escolha"
	}

	override fun getExamples(): List<String> {
		return listOf("pedra", "papel", "tesoura")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("sua escolha" to "Pedra, Papel ou Tesoura")
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val playerValue = context.args[0]

			val janken = Jankenpon.getFromLangString(playerValue.toLowerCase(), locale)

			if (janken != null) {
				val opponent = Jankenpon.values()[Loritta.RANDOM.nextInt(Jankenpon.values().size)]

				val status = janken.getStatus(opponent)

				var fancy: String? = null
				if (status == JankenponStatus.WIN) {
					fancy = "**${context.locale["commands.fun.rockpaperscissors.win"]} \uD83D\uDE0A**"
				}
				if (status == JankenponStatus.LOSE) {
					fancy = "**${context.locale["commands.fun.rockpaperscissors.lose"]} \uD83D\uDE42**"
				}
				if (status == JankenponStatus.DRAW) {
					fancy = "**${context.locale["commands.fun.rockpaperscissors.draw"]} \uD83D\uDE0A**"
				}
				if (fancy == null) {
					return
				}
				val prefix = when (status) {
					JankenponStatus.WIN -> "\uD83C\uDF89"
					JankenponStatus.DRAW -> "\uD83C\uDFF3"
					JankenponStatus.LOSE -> "\uD83C\uDFF4"
				}
				context.reply(
                        LorittaReply(message = context.locale["commands.fun.rockpaperscissors.chosen", janken.emoji, opponent.emoji], prefix = prefix),
                        LorittaReply(message = fancy, mentionUser = false)
				)
			} else {
				if (playerValue.equals("jesus", ignoreCase = true)) {
					val fancy = "**${context.locale["commands.fun.rockpaperscissors.maybeDraw"]} 🤔 🤷**"
					val jesus = "🙇 *${context.locale["commands.fun.rockpaperscissors.jesusChrist"]}* 🙇"
					context.reply(
                            LorittaReply(message = context.locale["commands.fun.rockpaperscissors.chosen", jesus, jesus], prefix = "\uD83C\uDFF3"),
                            LorittaReply(message = fancy, mentionUser = false)
					)
				} else {
					val fancy = "**${context.locale["commands.fun.rockpaperscissors.invalidChoice"]} \uD83D\uDE09**"
					val jesus = "🙇 *${context.locale["commands.fun.rockpaperscissors.jesusChrist"]}* 🙇"
					context.reply(
                            LorittaReply(message = context.locale["commands.fun.rockpaperscissors.chosen", "\uD83D\uDCA9", jesus], prefix = "\uD83C\uDFF4"),
                            LorittaReply(message = fancy, mentionUser = false)
					)
				}
			}
		} else {
			context.explain()
		}
	}
}