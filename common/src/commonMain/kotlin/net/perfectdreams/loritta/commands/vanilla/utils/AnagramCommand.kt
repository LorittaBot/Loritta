package net.perfectdreams.loritta.commands.vanilla.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.utils.declarations.AnagramCommandDeclaration
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.math.MathUtils

class AnagramCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(AnagramCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.anagram"
    }

    override suspend fun executes(context: CommandContext) {
        val currentWord = context.optionsManager.getString(AnagramCommandDeclaration.options.text)

        var shuffledChars = currentWord.toCharArray().toList()

        while (shuffledChars.size != 1 && shuffledChars.joinToString("") == currentWord && currentWord.groupBy { it }.size >= 2)
            shuffledChars = shuffledChars.shuffled()

        val shuffledWord = shuffledChars.joinToString(separator = "")

        var exp = 1.toBigInteger()
        currentWord.groupingBy { it }.eachCount().forEach { (_, value) ->
            exp = exp.multiply(MathUtils.factorial(value.toBigInteger()))
        }

        val max = MathUtils.factorial(currentWord.length.toBigInteger()).divide(exp)

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
    }
}