package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.DashboardExecutor

class DashboardCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Dashboard
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.MODERATION, I18N_PREFIX.Description) {
        executor = { DashboardExecutor(it) }
    }
}