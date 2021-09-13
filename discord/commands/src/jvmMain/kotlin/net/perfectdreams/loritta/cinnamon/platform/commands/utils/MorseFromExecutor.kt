package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class MorseFromExecutor(): CommandExecutor() {
    companion object : CommandExecutorDeclaration(MorseFromExecutor::class) {
        object Options : CommandOptions() {
            val textArgument = string("text", MorseCommand.I18N_PREFIX.Options.FromMorseToText)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val text = args[options.textArgument]

        when (val fromMorseResult = MorseUtils.fromMorse(text)) {
            is MorseUtils.ValidFromMorseConversionResult -> {
                val fromMorse = fromMorseResult.text
                val unknownMorseCodes = fromMorseResult.unknownMorseCodes

                context.sendMessage {
                    styled(
                        content = "`$fromMorse`",
                        prefix = Emotes.Radio.toString()
                    )

                    if (unknownMorseCodes.isNotEmpty()) {
                        styled(
                            content = context.i18nContext.get(
                                MorseCommand.I18N_PREFIX.FromMorseWarningUnknownMorseCodes(
                                    unknownMorseCodes.joinToString("")
                                )
                            ),
                            prefix = Emotes.LoriSob
                        )
                    }
                }
            }

            is MorseUtils.InvalidFromMorseConversionResult -> {
                context.failEphemerally(
                    prefix = Emotes.Error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.FromMorseFailUnknownMorseCodes
                    )
                )
            }
        }
    }
}