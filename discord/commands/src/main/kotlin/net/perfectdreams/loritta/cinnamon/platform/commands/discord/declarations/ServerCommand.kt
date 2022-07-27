package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.*

class ServerCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Server
    }

    override fun declaration() = slashCommand("server", CommandCategory.DISCORD, TodoFixThisData) {
        dmPermission = false

        subcommand("icon", I18N_PREFIX.Icon.Description) {
            executor = { ServerIconExecutor(it) }
        }

        subcommand("banner", I18N_PREFIX.Banner.Description) {
            executor = { ServerBannerExecutor(it) }
        }

        subcommand("splash", I18N_PREFIX.Splash.Description) {
            executor = { ServerSplashExecutor(it) }
        }

        subcommandGroup("channel", TodoFixThisData) {
            subcommand("info", I18N_PREFIX.Channel.Info.Description) {
                executor = { ChannelInfoExecutor(it) }
            }
        }

        subcommandGroup("role", TodoFixThisData) {
            subcommand("info", I18N_PREFIX.Role.Info.Description) {
                executor = { RoleInfoExecutor(it) }
            }
        }
    }
}