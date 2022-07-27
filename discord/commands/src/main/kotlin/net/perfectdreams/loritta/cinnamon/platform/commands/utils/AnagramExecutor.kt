package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.math.MathUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand

class AnagramExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val text = string("text", AnagramCommand.I18N_PREFIX.Options.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
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
                ) + " ${Emotes.LoriWow}",
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