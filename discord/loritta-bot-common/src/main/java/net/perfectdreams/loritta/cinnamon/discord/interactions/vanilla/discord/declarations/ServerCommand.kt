package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.*

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