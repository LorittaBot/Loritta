package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.FeatureFlags

class TranslateCommand : AbstractCommand("traduzir", listOf("translate"), CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["TRANSLATE_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "língua texto"
	}

	override fun getExamples(): List<String> {
		return listOf("pt Hello World!")
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (FeatureFlags.DISABLE_TRANSLATE_RATELIMIT) {
			context.reply(
					locale.toNewLocale()["commands.googleRateLimited", "${loritta.instanceConfig.loritta.website.url}${locale["website.localePath"]}/blog/youtube-google-block?utm_source=discord&utm_medium=link&utm_campaign=update_cmd"],
					Constants.ERROR
			)
			return
		}

		if (context.args.size >= 2) {
			val strLang = context.args[0]
			context.args[0] = "" // Super workaround
			val text = context.args.joinToString(" ")

			try {
				val translatedText = GoogleTranslateUtils.translate(text, "auto", strLang)

				context.reply(
						LoriReply(
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