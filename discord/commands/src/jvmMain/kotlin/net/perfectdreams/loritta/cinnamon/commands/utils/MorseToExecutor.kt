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

class MorseToExecutor(val emotes: Emotes) : CommandExecutor() {
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
                        prefix = emotes.radio.toString()
                    )

                    if (unknownCharacters.isNotEmpty()) {
                        styled(
                            content = context.i18nContext.get(
                                MorseCommand.I18N_PREFIX.ToMorseWarningUnknownCharacters(
                                    unknownCharacters.joinToString("")
                                )
                            ),
                            prefix = emotes.loriSob
                        )
                    }
                }
            }

            is MorseUtils.InvalidToMorseConversionResult -> {
                context.failEphemerally(
                    prefix = emotes.error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.ToMorseFailUnknownCharacters
                    )
                )
            }
        }
    }
}