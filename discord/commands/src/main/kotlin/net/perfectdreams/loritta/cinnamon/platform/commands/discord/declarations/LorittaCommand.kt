package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.LorittaInfoExecutor

object LorittaCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Loritta

    override fun declaration() = slashCommand(listOf("loritta"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("info"), I18N_PREFIX.Info.Description) {
            executor = LorittaInfoExecutor
        }
    }
}