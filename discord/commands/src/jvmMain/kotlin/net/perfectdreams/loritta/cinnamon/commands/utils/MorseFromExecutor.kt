package net.perfectdreams.loritta.cinnamon.commands.utils

import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled

class MorseFromExecutor(val emotes: Emotes): CommandExecutor() {
    companion object : CommandExecutorDeclaration(MorseFromExecutor::class) {
        object Options : CommandOptions() {
            val textArgument = string("text", MorseCommand.I18N_PREFIX.Options.FromMorseToText)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.textArgument]

        when (val fromMorseResult = MorseUtils.fromMorse(text)) {
            is MorseUtils.ValidFromMorseConversionResult -> {
                val fromMorse = fromMorseResult.text
                val unknownMorseCodes = fromMorseResult.unknownMorseCodes

                context.sendMessage {
                    styled(
                        content = "`$fromMorse`",
                        prefix = emotes.radio.toString()
                    )

                    if (unknownMorseCodes.isNotEmpty()) {
                        styled(
                            content = context.i18nContext.get(
                                MorseCommand.I18N_PREFIX.FromMorseWarningUnknownMorseCodes(
                                    unknownMorseCodes.joinToString("")
                                )
                            ),
                            prefix = emotes.loriSob
                        )
                    }
                }
            }

            is MorseUtils.InvalidFromMorseConversionResult -> {
                context.failEphemerally(
                    prefix = emotes.error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.FromMorseFailUnknownMorseCodes
                    )
                )
            }
        }
    }
}