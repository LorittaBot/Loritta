package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications.NotificationsListExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications.NotificationsViewExecutor

class NotificationsCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications
    }

    override fun declaration() = slashCommand("notifications", CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand("list", I18N_PREFIX.List.Description) {
            executor = { NotificationsListExecutor(it) }
        }

        subcommand("view", I18N_PREFIX.View.Description) {
            executor = { NotificationsViewExecutor(it) }
        }
    }
}