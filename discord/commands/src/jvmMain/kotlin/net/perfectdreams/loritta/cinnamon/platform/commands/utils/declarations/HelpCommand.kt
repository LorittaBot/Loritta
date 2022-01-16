package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.HelpExecutor

object HelpCommand: SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Help

    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand(listOf("help", "commands", "comandos"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = HelpExecutor
    }
}