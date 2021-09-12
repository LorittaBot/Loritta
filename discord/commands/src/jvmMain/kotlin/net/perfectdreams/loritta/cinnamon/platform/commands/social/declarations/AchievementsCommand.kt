package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor

object AchievementsCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Achievements

    override fun declaration() = command(listOf("achievements"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = AchievementsExecutor
    }
}