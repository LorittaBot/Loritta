package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.InviteInfoExecutor

object InviteCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Invite

    override fun declaration() = command(listOf("invite"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("info"), I18N_PREFIX.Info.Description) {
            executor = InviteInfoExecutor
        }
    }
}