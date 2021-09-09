package net.perfectdreams.loritta.cinnamon.commands.utils

import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils

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
                context.fail(
                    prefix = emotes.error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.FromMorseFailUnknownMorseCodes
                    )
                ) { isEphemeral = true }
            }
        }
    }
}