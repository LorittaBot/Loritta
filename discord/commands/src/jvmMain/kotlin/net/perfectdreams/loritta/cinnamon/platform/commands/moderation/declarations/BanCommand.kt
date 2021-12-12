package net.perfectdreams.loritta.cinnamon.platform.commands.moderation.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.BanInfoExecutor

object BanCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Ban

    override fun declaration() = command(listOf("ban"), CommandCategory.MODERATION, TodoFixThisData) {
        subcommand(listOf("info"), I18N_PREFIX.BanInfo.Description) {
            executor = BanInfoExecutor
        }
    }
}