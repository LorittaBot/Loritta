package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.text.MorseUtils
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

class MorseCommand : SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.UTILS,
            uniqueId = UUID.fromString("180c4180-6433-4c2f-ab96-fc385696ca12")
        ) {
            enableLegacyMessageSupport = true

            subcommand(
                name = I18N_PREFIX.FromMorseLabel,
                description = I18N_PREFIX.DescriptionFromMorse,
                uniqueId = UUID.fromString("5e888d0e-3327-4113-b3fa-b673920ace2a")
            ) {
                executor = FromMorseExecutor()
            }

            subcommand(
                name = I18N_PREFIX.ToMorseLabel,
                description = I18N_PREFIX.DescriptionToMorse,
                uniqueId = UUID.fromString("96a7f234-422a-4361-820f-c0a376866ce5")
            ) {
                executor = ToMorseExecutor()
            }
        }

    class FromMorseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Options.FromMorseToText)
        }

        override val options = Options()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            val text = args[options.text]

            when (val fromMorseResult = MorseUtils.fromMorse(text)) {
                is MorseUtils.ValidFromMorseConversionResult -> {
                    val fromMorse = fromMorseResult.text
                    val unknownMorseCodes = fromMorseResult.unknownMorseCodes

                    context.reply(ephemeral = false) {
                        styled(
                            content = "`$fromMorse`",
                            prefix = Emotes.Radio.toString()
                        )

                        if (unknownMorseCodes.isNotEmpty()) {
                            styled(
                                content = context.i18nContext.get(
                                    I18N_PREFIX.FromMorseWarningUnknownMorseCodes(
                                        unknownMorseCodes.joinToString("")
                                    )
                                ),
                                prefix = Emotes.LoriSob
                            )
                        }
                    }
                }

                is MorseUtils.InvalidFromMorseConversionResult -> {
                    context.fail(
                        emote = Emotes.Error.asMention,
                        text = context.i18nContext.get(
                            I18N_PREFIX.FromMorseFailUnknownMorseCodes
                        ),
                        ephemeral = true
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    class ToMorseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18N_PREFIX.Options.FromTextToMorse)
        }

        override val options = Options()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            val text = args[options.text]

            when (val toMorseResult = MorseUtils.toMorse(text)) {
                is MorseUtils.ValidToMorseConversionResult -> {
                    val toMorse = toMorseResult.morse
                    val unknownCharacters = toMorseResult.unknownCharacters

                    context.reply(ephemeral = false) {
                        styled(
                            content = "`$toMorse`",
                            prefix = Emotes.Radio.toString()
                        )

                        if (unknownCharacters.isNotEmpty()) {
                            styled(
                                content = context.i18nContext.get(
                                    I18N_PREFIX.ToMorseWarningUnknownCharacters(
                                        unknownCharacters.joinToString("")
                                    )
                                ),
                                prefix = Emotes.LoriSob
                            )
                        }
                    }
                }

                is MorseUtils.InvalidToMorseConversionResult -> {
                    context.fail(
                        emote = Emotes.Error.asMention,
                        text = context.i18nContext.get(
                            I18N_PREFIX.ToMorseFailUnknownCharacters
                        ),
                        ephemeral = true
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Morse
    }
}