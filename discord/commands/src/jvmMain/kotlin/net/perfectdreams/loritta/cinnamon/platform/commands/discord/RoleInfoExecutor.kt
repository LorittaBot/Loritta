package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.discordinteraktions.platforms.kord.entities.KordRole
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.ServerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.RawToFormated.toLocalized

class RoleInfoExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(RoleInfoExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val role = role("role", ServerCommand.I18N_PREFIX.Role.Info.Options.Role)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val role = args[Options.role] as KordRole

        val extension = if (role.handle.icon.value?.startsWith("a_") == true) "gif" else "png"
        val iconUrl = "https://cdn.discordapp.com/role-icons/${role.id.asString}/${role.handle.icon.value}.$extension?size=2048"

        // If the color is not 0, then it means that it has a color set!
        val hasColor = role.color != 0

        context.sendMessage {
            embed {
                title = "${Emotes.BriefCase.asMention} ${role.name}"
                color = if (hasColor)
                    Color(role.color)
                else
                    Color(114, 137, 218) // TODO: Move this to an object

                if (role.handle.icon.value != null)
                    thumbnailUrl = iconUrl


                field {
                    name = "${Emotes.Eyes} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Mention)
                    value = "`<@&${role.id.asString}>`"

                    inline = true
                }

                field {
                    name = "${Emotes.Computer} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.RoleId)
                    value = "`${role.id.asString}`"

                    inline = true
                }

                field {
                    name = "${Emotes.Eyes} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Hoisted)
                    value = context.i18nContext.get(role.hoist.toLocalized())

                    inline = true
                }

                field {
                    name = "${Emotes.BotTag} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Managed)
                    value = context.i18nContext.get(role.managed.toLocalized())

                    inline = true
                }

                field {
                    name = "${Emotes.LoriPing} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Mentionable)
                    value = context.i18nContext.get(role.mentionable.toLocalized())

                    inline = true
                }

                if (hasColor) {
                    field {
                        name = "${Emotes.Art} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Color)
                        value = "`#${Integer.toHexString(role.color).uppercase()}`"

                        inline = true
                    }
                }

                field {
                    name = "${Emotes.Date} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.CreatedAt)
                    value = "<t:${role.id.timestamp.toEpochMilliseconds() / 1000}:D>"

                    inline = true
                }

                val rolePermissionsLocalized = role.permissions.values.toLocalized()?.joinToString(
                    ", ",
                    transform = { "`${context.i18nContext.get(it)}`" }
                )

                field {
                    name = "${Emotes.Shield} " + context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.Permissions)
                    value = rolePermissionsLocalized ?: context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.NoPermissions)
                }
            }

            if (role.handle.icon.value != null)
                actionRow {
                    linkButton(iconUrl) {
                        label = context.i18nContext.get(ServerCommand.I18N_PREFIX.Role.Info.OpenRoleIconInBrowser)
                    }
                }
        }
    }
}