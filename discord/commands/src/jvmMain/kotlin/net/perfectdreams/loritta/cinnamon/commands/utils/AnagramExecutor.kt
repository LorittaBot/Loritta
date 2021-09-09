package net.perfectdreams.loritta.cinnamon.commands.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled

class AnagramExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AnagramExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", AnagramCommand.I18N_PREFIX.Options.Text)
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

        context.sendMessage {
            styled(
                content = context.i18nContext.get(
                    AnagramCommand.I18N_PREFIX.Result(
                        shuffledWord
                    )
                ) + " ${emotes.loriWow}",
                prefix = "✍"
            )

            styled(
                content = context.i18nContext.get(
                    AnagramCommand.I18N_PREFIX.Stats(
                        currentWord,
                        max // TODO: Can't format this as a number
                    )
                ),
                prefix = "\uD83E\uDD13"
            )
        }
    }
}