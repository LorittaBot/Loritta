package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.google.common.math.BigIntegerMath
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes


class AnagramaCommand : AbstractCommand("anagram", listOf("anagrama"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.anagram"
	}

	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
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