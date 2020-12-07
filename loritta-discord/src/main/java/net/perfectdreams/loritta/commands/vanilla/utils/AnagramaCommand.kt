package net.perfectdreams.loritta.commands.vanilla.utils

import com.google.common.math.BigIntegerMath
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

class AnagramaCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("anagrama", "anagram"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.anagram"
	}

	override fun command() = create {
		usage {
			argument(ArgumentType.TEXT) {}
		}

		localizedDescription("$LOCALE_PREFIX.description")

		executesDiscord {
			val context = this

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
				explain()
			}
		}
	}
}