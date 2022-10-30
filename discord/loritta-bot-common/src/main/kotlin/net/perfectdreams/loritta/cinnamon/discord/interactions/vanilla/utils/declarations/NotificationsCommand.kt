package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications.NotificationsListExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications.NotificationsViewExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

class NotificationsCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.List.Label, I18N_PREFIX.List.Description) {
            executor = { NotificationsListExecutor(it) }
        }

        subcommand(I18N_PREFIX.View.Label, I18N_PREFIX.View.Description) {
            executor = { NotificationsViewExecutor(it) }
        }
    }
}