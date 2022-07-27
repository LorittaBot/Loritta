package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.*

class ServerIconExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val guild = rest.guild.getGuild(context.guildId)

        val iconId = guild.icon ?: context.failEphemerally {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.Command.Server.Icon.NoIcon(Emotes.LoriPat)),
                Emotes.Error
            )
        }

        val extension = if (iconId.startsWith("a_")) "gif" else "png"
        val urlIcon = "https://cdn.discordapp.com/icons/${guild.id.value}/${guild.icon}.$extension?size=2048"

        context.sendMessage {
            embed {
                title = "${Emotes.Discord} ${guild.name}"
                image = urlIcon
                color = Color(114, 137, 218) // TODO: Move this to an object

                actionRow {
                    linkButton(
                        url = urlIcon
                    ) {
                        label = context.i18nContext.get(I18nKeysData.Commands.Command.Server.Icon.OpenIconInBrowser)
                    }
                }
            }
        }
    }
}