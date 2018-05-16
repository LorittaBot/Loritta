package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Jankenpon
import com.mrpowergamerbr.loritta.utils.Jankenpon.JankenponStatus
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.io.File
import java.io.IOException

class PedraPapelTesouraCommand : AbstractCommand("jankenpon", listOf("pedrapapeltesoura", "ppt"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PPT_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "sua escolha"
	}

	override fun getExample(): List<String> {
		return listOf("pedra", "papel", "tesoura")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("sua escolha" to "Pedra, Papel ou Tesoura")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val playerValue = context.args[0]

			val janken = Jankenpon.getFromLangString(playerValue.toLowerCase(), locale)

			if (janken != null) {
				val opponent = Jankenpon.values()[Loritta.RANDOM.nextInt(Jankenpon.values().size)]

				val status = janken.getStatus(opponent)

				var fancy: String? = null
				if (status == JankenponStatus.WIN) {
					fancy = "**${context.locale["PPT_WIN"]} \uD83D\uDE0A**"
				}
				if (status == JankenponStatus.LOSE) {
					fancy = "**${context.locale["PPT_LOSE"]} \uD83D\uDE42**"
				}
				if (status == JankenponStatus.DRAW) {
					fancy = "**${context.locale["PPT_DRAW"]} \uD83D\uDE0A**"
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
						LoriReply(message = context.locale["PPT_CHOSEN", janken.emoji, opponent.emoji], prefix = prefix),
						LoriReply(message = fancy, mentionUser = false)
				)
			} else {
				if (playerValue.equals("jesus", ignoreCase = true)) {
					val fancy = "**${context.locale["PPT_MAYBE_DRAW"]} 🤔 🤷**"
					val jesus = "🙇 *${context.locale["PPT_JESUS_CHRIST"]}* 🙇"
					context.reply(
							LoriReply(message = context.locale["PPT_CHOSEN", jesus, jesus], prefix = "\uD83C\uDFF3"),
							LoriReply(message = fancy, mentionUser = false)
					)
				} else if (playerValue.equals("velberan", ignoreCase = true)) {
					val opponent = Jankenpon.values()[Loritta.RANDOM.nextInt(Jankenpon.values().size)]

					val fancy = "🕹🕹🕹"
					context.sendMessage(context.getAsMention(true) + "Você escolheu 🕹 *VELBERAN*🕹, eu escolhi " + opponent.emoji + "\n" + fancy)
					try {
						context.sendFile(File(Loritta.ASSETS + "velberan.gif"), "velberan.gif", " ")
					} catch (e: IOException) {
						e.printStackTrace()
					}
				} else {
					val fancy = "**${context.locale["PPT_INVALID"]} \uD83D\uDE09**"
					val jesus = "🙇 *${context.locale["PPT_JESUS_CHRIST"]}* 🙇"
					context.reply(
							LoriReply(message = context.locale["PPT_CHOSEN", "\uD83D\uDCA9", jesus], prefix = "\uD83C\uDFF4"),
							LoriReply(message = fancy, mentionUser = false)
					)
				}
			}
		} else {
			context.explain()
		}
	}
}