package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class ChooseCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Choose
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label,
        I18N_PREFIX.Description,
        CommandCategory.UTILS,
        UUID.fromString("8113273c-df2e-4e04-812a-ecae4b2273cc")
    ) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        this.enableLegacyMessageSupport = true

        executor = ChooseExecutor()
    }

    inner class ChooseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val choices = (0 until 25).map {
                // Sneaky!
                if (it in 0..1) {
                    string("choice${it + 1}", I18N_PREFIX.Options.Choice)
                } else {
                    optionalString("choice${it + 1}", I18N_PREFIX.Options.Choice)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val validChoices = mutableListOf<String>()
            for (choiceOption in options.choices) {
                val result = args[choiceOption]
                if (result != null)
                    validChoices.add(result)
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Result(
                            result = cleanUpForOutput(context, validChoices.random()),
                            emote = Emotes.LoriYay
                        )
                    ),
                    Emotes.LoriHm
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val choices = args.joinToString(" ").split(",").map { it.trim() }

            return options.choices.mapIndexed { index, stringDiscordOptionReference ->
                stringDiscordOptionReference to choices.getOrNull(index)
            }.toMap()
        }
    }
}