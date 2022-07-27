package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.social.AfkOffExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AfkOnExecutor

class AfkCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Afk
    }

    override fun declaration() = slashCommand("afk", CommandCategory.SOCIAL, TodoFixThisData) {
        subcommand("on", I18N_PREFIX.On.Description) {
            executor = AfkOnExecutor(loritta)
        }

        subcommand("off", I18N_PREFIX.Off.Description) {
            executor = AfkOffExecutor(loritta)
        }
    }
}