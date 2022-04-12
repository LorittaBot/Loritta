package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse.MorseToExecutor

object MorseCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Morse

    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand(listOf("morse"), CommandCategory.UTILS,  I18N_PREFIX.Description) {
        subcommand(listOf("to"), I18N_PREFIX.DescriptionToMorse) {
            executor = MorseToExecutor
        }
        subcommand(listOf("from"), I18N_PREFIX.DescriptionFromMorse) {
            executor = MorseFromExecutor
        }
    }
}