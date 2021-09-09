package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object MorseCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Morse

    override fun declaration(): CommandDeclarationBuilder = command(listOf("morse"), CommandCategory.UTILS,  I18N_PREFIX.Description) {
        subcommand(listOf("to"), I18N_PREFIX.DescriptionToMorse) {
            executor = MorseToExecutor
        }
        subcommand(listOf("from"), I18N_PREFIX.DescriptionFromMorse) {
            executor = MorseFromExecutor
        }
    }
}