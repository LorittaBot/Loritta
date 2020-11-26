package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils
import net.perfectdreams.loritta.api.commands.CommandCategory

class TranslateCommand : AbstractCommand("traduzir", listOf("translate"), CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.utils.translate.description"]
	}

	override fun getUsage(): String {
		return "língua texto"
	}

	override fun getExamples(): List<String> {
		return listOf("pt Hello World!")
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
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