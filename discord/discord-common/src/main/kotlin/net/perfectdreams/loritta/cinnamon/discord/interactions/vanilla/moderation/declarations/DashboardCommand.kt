package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.DashboardExecutor

class DashboardCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Dashboard
    }

    override fun declaration() = slashCommand("dashboard", CommandCategory.MODERATION, I18N_PREFIX.Description) {
        executor = { DashboardExecutor(it) }
    }
}