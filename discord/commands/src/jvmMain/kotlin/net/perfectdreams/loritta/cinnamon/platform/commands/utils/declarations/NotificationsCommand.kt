package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsListExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.NotificationsViewExecutor

object NotificationsCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Package

    override fun declaration() = slashCommand(listOf("notifications"), CommandCategory.UTILS, TodoFixThisData) {
        subcommand(listOf("list"), TodoFixThisData) {
            executor = NotificationsListExecutor
        }

        subcommand(listOf("view"), TodoFixThisData) {
            executor = NotificationsViewExecutor
        }
    }
}