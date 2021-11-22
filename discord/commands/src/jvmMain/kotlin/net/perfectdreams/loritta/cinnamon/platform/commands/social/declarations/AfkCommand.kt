package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AfkOffExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AfkOnExecutor

object AfkCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Afk

    override fun declaration() = command(listOf("afk"), CommandCategory.SOCIAL, TodoFixThisData) {
        subcommand(listOf("on"), I18N_PREFIX.On.Description) {
            executor = AfkOnExecutor
        }

        subcommand(listOf("off"), I18N_PREFIX.Off.Description) {
            executor = AfkOffExecutor
        }
    }
}