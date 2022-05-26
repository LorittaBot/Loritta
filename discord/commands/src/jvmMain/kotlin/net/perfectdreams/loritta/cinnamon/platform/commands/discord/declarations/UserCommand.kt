package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.UserAvatarSlashExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.UserInfoSlashExecutor

object UserCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = slashCommand(listOf("user"), CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommand(listOf("avatar"), I18N_PREFIX.Avatar.Description) {
            executor = UserAvatarSlashExecutor
        }

        subcommand(listOf("info"), I18N_PREFIX.Info.Description) {
            executor = UserInfoSlashExecutor
        }

        subcommand(listOf("banner"), I18N_PREFIX.Banner.Description) {
            executor = UserBannerExecutor
        }
    }
}