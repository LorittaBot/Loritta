package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.UserAvatarSlashExecutor

object UserCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = slashCommand(listOf("user"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("avatar"), I18nKeysData.Commands.Command.User.Avatar.Description) {
            executor = UserAvatarSlashExecutor
        }

        subcommand(listOf("banner"), I18nKeysData.Commands.Command.User.Banner.Description) {
            executor = UserBannerExecutor
        }
    }
}