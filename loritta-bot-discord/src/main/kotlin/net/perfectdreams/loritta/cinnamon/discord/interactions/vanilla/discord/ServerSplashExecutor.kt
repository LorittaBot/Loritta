package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.*
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.common.utils.LorittaColors

class ServerSplashExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val guild = rest.guild.getGuild(context.guildId)

        val splashId = guild.splash.value ?: context.failEphemerally {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.Command.Server.Splash.NoSplash(Emotes.LoriPat)),
                Emotes.LoriSob
            )
        }

        val extension = if (splashId.startsWith("a_")) "gif" else "png"
        val urlIcon = "https://cdn.discordapp.com/splashes/${guild.id.value}/${guild.splash.value}.$extension?size=2048"

        context.sendMessage {
            embed {
                title = "${Emotes.Discord} ${guild.name}"
                image = urlIcon
                color = LorittaColors.DiscordBlurple.toKordColor()

                actionRow {
                    linkButton(
                        url = urlIcon
                    ) {
                        label = context.i18nContext.get(I18nKeysData.Commands.Command.Server.Splash.OpenSplashInBrowser)
                    }
                }
            }
        }
    }
}