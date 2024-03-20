package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class AnagramCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("anagrama")
        }

        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        examples = I18N_PREFIX.Examples

        executor = AnagramExecutor()
    }

    inner class AnagramExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
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

            context.reply(false) {
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

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(separator = " ")
            )
        }
    }
}