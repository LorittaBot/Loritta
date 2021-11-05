package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ChannelInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.RoleInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerIconExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerSplashExecutor

object ServerCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Server

    override fun declaration() = command(listOf("server"), CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(listOf("icon"), I18nKeysData.Commands.Command.Server.Icon.Description) {
            executor = ServerIconExecutor
        }

        subcommand(listOf("banner"), I18nKeysData.Commands.Command.Server.Banner.Description) {
            executor = ServerBannerExecutor
        }

        subcommand(listOf("splash"), I18nKeysData.Commands.Command.Server.Splash.Description) {
            executor = ServerSplashExecutor
        }

        subcommandGroup(listOf("channel"), TodoFixThisData) {
            subcommand(listOf("info"), I18nKeysData.Commands.Command.Server.Channel.Info.Description) {
                executor = ChannelInfoExecutor
            }
        }

        subcommandGroup(listOf("role"), TodoFixThisData) {
            subcommand(listOf("info"), I18nKeysData.Commands.Command.Server.Role.Info.Description) {
                executor = RoleInfoExecutor
            }
        }
    }
}