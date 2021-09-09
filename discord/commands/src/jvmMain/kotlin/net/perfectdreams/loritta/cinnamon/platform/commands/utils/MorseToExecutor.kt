package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class MorseToExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(MorseToExecutor::class) {
        object Options : CommandOptions() {
            val textArgument = string("text", MorseCommand.I18N_PREFIX.Options.FromTextToMorse)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.textArgument]

        when (val toMorseResult = MorseUtils.toMorse(text)) {
            is MorseUtils.ValidToMorseConversionResult -> {
                val toMorse = toMorseResult.morse
                val unknownCharacters = toMorseResult.unknownCharacters

                context.sendMessage {
                    styled(
                        content = "`$toMorse`",
                        prefix = Emotes.radio.toString()
                    )

                    if (unknownCharacters.isNotEmpty()) {
                        styled(
                            content = context.i18nContext.get(
                                MorseCommand.I18N_PREFIX.ToMorseWarningUnknownCharacters(
                                    unknownCharacters.joinToString("")
                                )
                            ),
                            prefix = Emotes.loriSob
                        )
                    }
                }
            }

            is MorseUtils.InvalidToMorseConversionResult -> {
                context.failEphemerally(
                    prefix = Emotes.error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.ToMorseFailUnknownCharacters
                    )
                )
            }
        }
    }
}