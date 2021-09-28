package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.*
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration

class ServerBannerExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ServerBannerExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext) throw CommandException {
            content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
        }

        val guild = rest.guild.getGuild(context.guildId)

        val bannerId = guild.banner ?: context.failEphemerally {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.Command.Server.Banner.NoBanner),
                Emotes.LoriSob
            )
        }

        val extension = if (bannerId.startsWith("a_")) "gif" else "png"
        val bannerUrl = "https://cdn.discordapp.com/banners/${guild.id.value}/${guild.banner}.$extension?size=2048"

        context.sendMessage {
            embed {
                title = "${Emotes.Discord} ${guild.name}"
                color = Color(114, 137, 218) // TODO: Move this to an object

                image = bannerUrl

                actionRow {
                    linkButton(
                        url = bannerUrl
                    ) {
                        label = context.i18nContext.get(I18nKeysData.Commands.Command.User.Banner.OpenBannerInBrowser)
                    }
                }
            }
        }
    }
}