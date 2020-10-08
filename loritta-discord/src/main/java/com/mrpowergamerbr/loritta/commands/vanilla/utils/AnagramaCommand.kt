package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.google.common.math.BigIntegerMath
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes


class AnagramaCommand : AbstractCommand("anagram", listOf("anagrama"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.anagram"
	}

	override fun getUsage(): String {
		return "palavra"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.description"]
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("Loritta" to "Cria um anagrama usando a palavra \"Loritta\"",
				"kk eae men" to "Cria um anagrama usando a frase \"kk eae men\"")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val currentWord = context.args.joinToString(separator = " ")

			var shuffledChars = currentWord.toCharArray().toList()

			while (shuffledChars.size != 1 && shuffledChars.joinToString("") == currentWord && currentWord.groupBy { it }.size >= 2)
				shuffledChars = shuffledChars.shuffled()

			val shuffledWord = shuffledChars.joinToString(separator = "")

			var exp = 1.toBigInteger()
			currentWord.groupingBy { it }.eachCount().forEach { (_, value) ->
				exp = exp.multiply(BigIntegerMath.factorial(value))
			}

			val max = BigIntegerMath.factorial(currentWord.length).divide(exp)

			context.reply(
                    LorittaReply(
                            message = context.locale["$LOCALE_PREFIX.result", shuffledWord] + " ${Emotes.LORI_WOW}",
                            prefix = "✍"
                    ),
                    LorittaReply(
                            message = context.locale["$LOCALE_PREFIX.stats", currentWord, max],
                            prefix = "\uD83E\uDD13"
                    )
			)
		} else {
			this.explain(context)
		}
	}
}