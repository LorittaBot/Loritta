package net.perfectdreams.loritta.cinnamon.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.commands.utils.HelpExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object HelpCommand: CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Help

    override fun declaration(): CommandDeclarationBuilder = command(listOf("help", "commands", "comandos"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = HelpExecutor
    }
}