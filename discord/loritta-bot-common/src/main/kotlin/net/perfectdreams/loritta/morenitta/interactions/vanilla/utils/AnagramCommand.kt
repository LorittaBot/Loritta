package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

class AnagramCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS) {
        executor = AnagramExecutor()
    }

    inner class AnagramExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Options.Text)
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

            context.reply {
                styled(
                    content = context.i18nContext.get(
                        I18N_PREFIX.Result(
                            cleanUpForOutput(context, shuffledWord)
                        )
                    ) + " ${Emotes.LoriWow}",
                    prefix = "✍"
                )

                styled(
                    content = context.i18nContext.get(
                        I18N_PREFIX.Stats(
                            cleanUpForOutput(context, currentWord),
                            max // TODO: Can't format this as a number
                        )
                    ),
                    prefix = "\uD83E\uDD13"
                )
            }
        }
    }
}