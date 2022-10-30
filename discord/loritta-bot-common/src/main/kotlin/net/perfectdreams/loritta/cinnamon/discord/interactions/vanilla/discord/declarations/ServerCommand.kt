package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.RoleInfoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.ServerBannerExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.ServerIconExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.ServerSplashExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData

class ServerCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Server
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, TodoFixThisData) {
        dmPermission = false

        subcommand(I18N_PREFIX.Icon.Label, I18N_PREFIX.Icon.Description) {
            executor = { ServerIconExecutor(it) }
        }

        subcommand(I18N_PREFIX.Banner.Label, I18N_PREFIX.Banner.Description) {
            executor = { ServerBannerExecutor(it) }
        }

        subcommand(I18N_PREFIX.Splash.Label, I18N_PREFIX.Splash.Description) {
            executor = { ServerSplashExecutor(it) }
        }

        subcommandGroup(I18N_PREFIX.Channel.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Channel.Info.Label, I18N_PREFIX.Channel.Info.Description) {
                executor = {
                    net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.ChannelInfoExecutor(
                        it
                    )
                }
            }
        }

        subcommandGroup(I18N_PREFIX.Role.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Role.Info.Label, I18N_PREFIX.Role.Info.Description) {
                executor = { RoleInfoExecutor(it) }
            }
        }
    }
}