package net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand

class MorseFromExecutor(): SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val textArgument = string("text", MorseCommand.I18N_PREFIX.Options.FromMorseToText)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
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