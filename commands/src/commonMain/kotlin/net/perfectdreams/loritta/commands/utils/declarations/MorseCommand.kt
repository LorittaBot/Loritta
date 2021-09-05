package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.i18n.I18nKeysData

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