package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsListExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsViewExecutor

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