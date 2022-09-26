package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.translate.GoogleTranslateUtils
import net.perfectdreams.loritta.common.messages.LorittaReply

class TranslateCommand : AbstractCommand("traduzir", listOf("translate"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.translate.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.translate.examples")

	// TODO: Fix Usage

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size >= 2) {
			val strLang = context.args[0]
			context.args[0] = "" // Super workaround
			val text = context.args.joinToString(" ")

			try {
				val translatedText = GoogleTranslateUtils.translate(text, "auto", strLang)

				context.reply(
                        LorittaReply(
                                translatedText!!.escapeMentions(),
                                "\uD83D\uDDFA"
                        )
				)
			} catch (e: Exception) {
				logger.warn(e) { "Error while translating $text to $strLang!" }
			}
		} else {
			context.explain()
		}
	}
}