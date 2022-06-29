package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsListExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsViewExecutor

object NotificationsCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Notifications

    override fun declaration() = slashCommand(listOf("notifications"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(listOf("list"), I18N_PREFIX.List.Description) {
            executor = NotificationsListExecutor
        }

        subcommand(listOf("view"), I18N_PREFIX.View.Description) {
            executor = NotificationsViewExecutor
        }
    }
}