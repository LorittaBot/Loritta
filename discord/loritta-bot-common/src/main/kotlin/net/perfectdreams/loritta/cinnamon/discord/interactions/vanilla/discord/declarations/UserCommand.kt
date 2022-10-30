package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar.UserAvatarSlashExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info.UserInfoSlashExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

class UserCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.User
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.Avatar.Label, I18N_PREFIX.Avatar.Description) {
            executor = { UserAvatarSlashExecutor(it) }
        }

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description) {
            executor = { UserInfoSlashExecutor(it, it.http) }
        }

        subcommand(I18N_PREFIX.Banner.Label, I18N_PREFIX.Banner.Description) {
            executor = { UserBannerExecutor(it) }
        }
    }
}