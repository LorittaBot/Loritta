package net.perfectdreams.loritta.commands.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.perfectdreams.loritta.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.math.MathUtils

class AnagramExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AnagramExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", LocaleKeyData("commands.command.anagram.options.text"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val currentWord = args[options.text]

        var shuffledChars = currentWord.toCharArray().toList()

        while (shuffledChars.size != 1 && shuffledChars.joinToString("") == currentWord && currentWord.groupBy { it }.size >= 2)
            shuffledChars = shuffledChars.shuffled()

        val shuffledWord = shuffledChars.joinToString(separator = "")

        var exp = 1.toBigInteger()
        currentWord.groupingBy { it }.eachCount().forEach { (_, value) ->
            exp = exp.multiply(MathUtils.factorial(value.toBigInteger()))
        }

        val max = MathUtils.factorial(currentWord.length.toBigInteger()).divide(exp)

        context.sendReplies {
            styled(
                content = context.locale["${AnagramCommand.LOCALE_PREFIX}.result", shuffledWord] + " ${emotes.loriWow}",
                prefix = "✍",
                mentionSenderHint = true
            )

            styled(
                content = context.locale["${AnagramCommand.LOCALE_PREFIX}.stats", currentWord, max],
                prefix = "\uD83E\uDD13"
            )
        }
    }
}