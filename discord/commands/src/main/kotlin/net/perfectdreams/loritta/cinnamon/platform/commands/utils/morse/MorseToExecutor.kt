package net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.MorseUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand

class MorseToExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val textArgument = string("text", MorseCommand.I18N_PREFIX.Options.FromTextToMorse)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.textArgument]

        when (val toMorseResult = MorseUtils.toMorse(text)) {
            is MorseUtils.ValidToMorseConversionResult -> {
                val toMorse = toMorseResult.morse
                val unknownCharacters = toMorseResult.unknownCharacters

                context.sendMessage {
                    styled(
                        content = "`$toMorse`",
                        prefix = Emotes.Radio.toString()
                    )

                    if (unknownCharacters.isNotEmpty()) {
                        styled(
                            content = context.i18nContext.get(
                                MorseCommand.I18N_PREFIX.ToMorseWarningUnknownCharacters(
                                    unknownCharacters.joinToString("")
                                )
                            ),
                            prefix = Emotes.LoriSob
                        )
                    }
                }
            }

            is MorseUtils.InvalidToMorseConversionResult -> {
                context.failEphemerally(
                    prefix = Emotes.Error.asMention,
                    content = context.i18nContext.get(
                        MorseCommand.I18N_PREFIX.ToMorseFailUnknownCharacters
                    )
                )
            }
        }
    }
}