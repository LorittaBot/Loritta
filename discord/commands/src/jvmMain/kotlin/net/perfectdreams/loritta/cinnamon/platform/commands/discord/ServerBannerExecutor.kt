package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class ServerBannerExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val guild = rest.guild.getGuild(context.guildId)

        val bannerId = guild.banner ?: context.failEphemerally {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.Command.Server.Banner.NoBanner(Emotes.LoriPat)),
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
                        label = context.i18nContext.get(I18nKeysData.Commands.Command.Server.Banner.OpenBannerInBrowser)
                    }
                }
            }
        }
    }
}