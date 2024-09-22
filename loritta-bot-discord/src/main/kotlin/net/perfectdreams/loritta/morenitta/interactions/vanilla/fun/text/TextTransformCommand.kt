package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.text

import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.UUID

class TextTransformCommand: SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = TodoFixThisData,
            category = CommandCategory.FUN,
            uniqueId = UUID.fromString("01bf4a78-1aaa-45c9-bf83-72f1742a3498")
        ) {
            enableLegacyMessageSupport = true

            for (simpleSubcommand in simpleSubcommands) {
                subcommand(simpleSubcommand.label, simpleSubcommand.description, simpleSubcommand.uniqueId) {
                    executor = SimpleTextTransformCommandExecutor(simpleSubcommand)
                }
            }
            subcommand(
                name = TextClapExecutor.I18N_PREFIX.Label,
                description = TextClapExecutor.I18N_PREFIX.Description(TextClapExecutor.CLAP_EMOJI),
                uniqueId = UUID.fromString("15af7f54-6fb1-48b1-8e22-6e0976b0d621")
            ) {
                executor = TextClapExecutor()
            }
            subcommand(
                name = TextVemDeZapExecutor.I18N_PREFIX.Label,
                description = TextVemDeZapExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("da3ac3a6-16f8-42d0-92d3-e0a9859d7ad4")
            ) {
                executor = TextVemDeZapExecutor()
            }
        }

    class SimpleTextTransformCommandExecutor(
        val simpleSubcommand: SimpleTextTransformSubcommand
    ): LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options: ApplicationCommandOptions() {
            val text = string("text", simpleSubcommand.textOption)
        }

        override val options: Options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])
            val transformedText = simpleSubcommand.transform(text)
            context.reply(ephemeral = false) {
                styled(
                    content = transformedText,
                    prefix = "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) return null

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    class TextClapExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options: ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Options.Text(CLAP_EMOJI))
            val emoji = optionalString("emoji", I18N_PREFIX.Options.Emoji)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val text = cleanUpForOutput(context, args[options.text])
            val emoji = args[options.emoji] ?: CLAP_EMOJI

            context.reply(ephemeral = false) {
                styled(
                    content = "$emoji${text.split(" ").joinToString(emoji)}$emoji",
                    prefix = "✍"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) return null

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }

        companion object {
            val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Clap
            val CLAP_EMOJI = "\uD83D\uDC4F"
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Text

        val simpleSubcommands = listOf(
            TextTransformVaporwaveSubcommand(),
            TextTransformUppercaseSubcommand(),
            TextTransformLowercaseSubcommand(),
            TextTransformMockSubcommand(),
            TextTransformQualitySubcommand(),
            TextTransformVaporQualitySubcommand()
        )
    }
}