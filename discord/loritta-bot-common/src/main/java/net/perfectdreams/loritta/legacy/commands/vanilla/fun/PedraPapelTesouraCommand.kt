package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Jankenpon
import net.perfectdreams.loritta.legacy.utils.Jankenpon.JankenponStatus
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class PedraPapelTesouraCommand : AbstractCommand("jankenpon", listOf("pedrapapeltesoura", "ppt"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.rockpaperscissors.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.rockpaperscissors.examples")

	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "jankenpon")

		if (context.args.isNotEmpty()) {
			val playerValue = context.args[0]

			val janken = Jankenpon.getFromLangString(playerValue.toLowerCase(), locale)

			if (janken != null) {
				val opponent = Jankenpon.values()[Loritta.RANDOM.nextInt(Jankenpon.values().size)]

				val status = janken.getStatus(opponent)

				var fancy: String? = null
				if (status == JankenponStatus.WIN) {
					fancy = "**${context.locale["commands.command.rockpaperscissors.win"]} \uD83D\uDE0A**"
				}
				if (status == JankenponStatus.LOSE) {
					fancy = "**${context.locale["commands.command.rockpaperscissors.lose"]} \uD83D\uDE42**"
				}
				if (status == JankenponStatus.DRAW) {
					fancy = "**${context.locale["commands.command.rockpaperscissors.draw"]} \uD83D\uDE0A**"
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
                        LorittaReply(message = context.locale["commands.command.rockpaperscissors.chosen", janken.emoji, opponent.emoji], prefix = prefix),
                        LorittaReply(message = fancy, mentionUser = false)
				)
			} else {
				if (playerValue.equals("jesus", ignoreCase = true)) {
					val fancy = "**${context.locale["commands.command.rockpaperscissors.maybeDraw"]} 🤔 🤷**"
					val jesus = "🙇 *${context.locale["commands.command.rockpaperscissors.jesusChrist"]}* 🙇"
					context.reply(
                            LorittaReply(message = context.locale["commands.command.rockpaperscissors.chosen", jesus, jesus], prefix = "\uD83C\uDFF3"),
                            LorittaReply(message = fancy, mentionUser = false)
					)
				} else {
					val fancy = "**${context.locale["commands.command.rockpaperscissors.invalidChoice"]} \uD83D\uDE09**"
					val jesus = "🙇 *${context.locale["commands.command.rockpaperscissors.jesusChrist"]}* 🙇"
					context.reply(
                            LorittaReply(message = context.locale["commands.command.rockpaperscissors.chosen", "\uD83D\uDCA9", jesus], prefix = "\uD83C\uDFF4"),
                            LorittaReply(message = fancy, mentionUser = false)
					)
				}
			}
		} else {
			context.explain()
		}
	}
}