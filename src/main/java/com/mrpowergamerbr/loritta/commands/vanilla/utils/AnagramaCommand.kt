package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.google.common.math.BigIntegerMath
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale


class AnagramaCommand : AbstractCommand("anagram", listOf("anagrama"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "palavra"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["ANAGRAMA_DESCRIPTION"]
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("Loritta" to "Cria um anagrama usando a palavra \"Loritta\"",
				"kk eae men" to "Cria um anagrama usando a frase \"kk eae men\"")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val palavra = context.args.joinToString(separator = " ");

			val shuffledChars = LorittaUtilsKotlin.shuffle(palavra.toCharArray().toMutableList())

			val shuffledWord = shuffledChars.joinToString(separator = "");

			val chars = mutableMapOf<Char, Int>()
			for (ch in palavra) {
				chars.put(ch, chars.getOrDefault(ch, 0) + 1)
			}

			var exp = 1.toBigInteger()
			for ((_, value) in chars.entries) {
				exp = exp.multiply(BigIntegerMath.factorial(value))
			}

			val max = BigIntegerMath.factorial(palavra.length).divide(exp)

			context.reply(
					LoriReply(
							message = context.locale["ANAGRAMA_RESULT", shuffledWord] + " \uD83D\uDE4B",
							prefix = "✍"
					),
					LoriReply(
							message = context.locale["ANAGRAMA_Stats", palavra, max],
							prefix = "\uD83E\uDD13"
					)
			)
		} else {
			this.explain(context);
		}
	}
}