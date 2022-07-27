package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.InviteInfoExecutor

class InviteCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Invite
    }

    override fun declaration() = slashCommand("invite", CommandCategory.DISCORD, TodoFixThisData) {
        subcommand("info", I18N_PREFIX.Info.Description) {
            executor = InviteInfoExecutor(loritta)
        }
    }
}