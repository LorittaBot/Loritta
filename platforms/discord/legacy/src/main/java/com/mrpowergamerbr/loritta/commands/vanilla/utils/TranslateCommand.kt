package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class TranslateCommand : AbstractCommand("traduzir", listOf("translate"), CommandCategory.UTILS) {
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