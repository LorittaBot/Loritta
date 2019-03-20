package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Jankenpon
import com.mrpowergamerbr.loritta.utils.Jankenpon.JankenponStatus
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.io.File
import java.io.IOException

class PedraPapelTesouraCommand : AbstractCommand("jankenpon", listOf("pedrapapeltesoura", "ppt"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PPT_DESCRIPTION"]
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val playerValue = context.args[0]

			val janken = Jankenpon.getFromLangString(playerValue.toLowerCase(), locale)

			if (janken != null) {
				val opponent = Jankenpon.values()[Loritta.RANDOM.nextInt(Jankenpon.values().size)]

				val status = janken.getStatus(opponent)

				var fancy: String? = null
				if (status == JankenponStatus.WIN) {
					fancy = "**${context.legacyLocale["PPT_WIN"]} \uD83D\uDE0A**"
				}
				if (status == JankenponStatus.LOSE) {
					fancy = "**${context.legacyLocale["PPT_LOSE"]} \uD83D\uDE42**"
				}
				if (status == JankenponStatus.DRAW) {
					fancy = "**${context.legacyLocale["PPT_DRAW"]} \uD83D\uDE0A**"
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
						LoriReply(message = context.legacyLocale["PPT_CHOSEN", janken.emoji, opponent.emoji], prefix = prefix),
						LoriReply(message = fancy, mentionUser = false)
				)
			} else {
				if (playerValue.equals("jesus", ignoreCase = true)) {
					val fancy = "**${context.legacyLocale["PPT_MAYBE_DRAW"]} 🤔 🤷**"
					val jesus = "🙇 *${context.legacyLocale["PPT_JESUS_CHRIST"]}* 🙇"
					context.reply(
							LoriReply(message = context.legacyLocale["PPT_CHOSEN", jesus, jesus], prefix = "\uD83C\uDFF3"),
							LoriReply(message = fancy, mentionUser = false)
					)
				} else {
					val fancy = "**${context.legacyLocale["PPT_INVALID"]} \uD83D\uDE09**"
					val jesus = "🙇 *${context.legacyLocale["PPT_JESUS_CHRIST"]}* 🙇"
					context.reply(
							LoriReply(message = context.legacyLocale["PPT_CHOSEN", "\uD83D\uDCA9", jesus], prefix = "\uD83C\uDFF4"),
							LoriReply(message = fancy, mentionUser = false)
					)
				}
			}
		} else {
			context.explain()
		}
	}
}