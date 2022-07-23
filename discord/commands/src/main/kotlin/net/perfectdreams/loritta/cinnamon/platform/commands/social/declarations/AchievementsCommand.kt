package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor

object AchievementsCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Achievements

    override fun declaration() = slashCommand(listOf("achievements"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = AchievementsExecutor
    }
}