package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.EmojiInfoExecutor

object EmojiCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Emoji

    override fun declaration() = command(listOf("emoji"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("info"), I18nKeysData.Commands.Command.Emoji.Info.Description) {
            executor = EmojiInfoExecutor
        }
    }
}