package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor

class AchievementsCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Achievements
    }

    override fun declaration() = slashCommand("achievements", CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = AchievementsExecutor(loritta)
    }
}