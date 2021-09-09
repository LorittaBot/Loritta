package net.perfectdreams.loritta.cinnamon.discord.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.discord.commands.discord.UserAvatarExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object UserCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = command(listOf("user"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("avatar"), I18nKeysData.Commands.Command.User.Avatar.Description) {
            executor = UserAvatarExecutor
        }

        subcommand(listOf("banner"), I18nKeysData.Commands.Command.User.Banner.Description) {
            executor = UserBannerExecutor
        }
    }
}