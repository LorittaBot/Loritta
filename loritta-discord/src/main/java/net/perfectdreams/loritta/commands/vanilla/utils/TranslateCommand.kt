package net.perfectdreams.loritta.commands.vanilla.utils

import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class TranslateCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("traduzir", "translate"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.translate"
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		examples {
			+ "pt Hello World!"
		}

		executesDiscord {
			val context = this

			if (context.args.size >= 2) {
				val strLang = context.args.getOrNull(0) ?: ""
				val text = context.args.drop(1).joinToString(" ")

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
}