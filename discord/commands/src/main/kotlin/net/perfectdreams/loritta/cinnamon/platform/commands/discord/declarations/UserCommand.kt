package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.UserAvatarSlashExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.UserInfoSlashExecutor

class UserCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.User
    }

    override fun declaration() = slashCommand("user", CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommand("avatar", I18N_PREFIX.Avatar.Description) {
            executor = UserAvatarSlashExecutor(loritta)
        }

        subcommand("info", I18N_PREFIX.Info.Description) {
            executor = UserInfoSlashExecutor(loritta, loritta.http)
        }

        subcommand("banner", I18N_PREFIX.Banner.Description) {
            executor = UserBannerExecutor(loritta)
        }
    }
}