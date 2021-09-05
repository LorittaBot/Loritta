package net.perfectdreams.loritta.discord.commands.discord

import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.discord.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.platform.discord.commands.discord.DiscordCommandExecutor
import java.awt.Color

class UserBannerExecutor(val emotes: Emotes, val rest: RestClient) : DiscordCommandExecutor() {
    companion object : CommandExecutorDeclaration(UserBannerExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", UserCommand.I18N_PREFIX.Banner.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun executeDiscord(context: DiscordCommandContext, args: CommandArguments) {
        val user = args[Options.user] ?: context.user

        // We need to retrieve from Discord's API to get the banner info
        // Also, this is the reason why this command is in the Discord InteraKTions platform
        val retrievedDiscordUser = rest.user.getUser(Snowflake(user.id))

        val bannerId = retrievedDiscordUser.banner ?: context.fail {
            isEphemeral = true
            styled(
                context.i18nContext.get(
                    I18nKeysData.Commands.Command.User.Banner.UserDoesNotHaveAnBanner(mentionUser(user, false))
                ),
                prefix = emotes.error
            )
        }

        val extension = if (bannerId.startsWith("a_")) "gif" else "png"
        val bannerUrl = "https://cdn.discordapp.com/banners/${user.id}/${retrievedDiscordUser.banner}.$extension?size=512"

        context.sendMessage {
            embed {
                title = "\uD83D\uDDBC ${user.name}"
                description = "**${context.i18nContext.get(UserCommand.I18N_PREFIX.Banner.ClickHere(bannerUrl))}**"

                image(bannerUrl)

                // Easter Egg: Looking up yourself
                if (context.user.id == user.id)
                    footer(context.i18nContext.get(UserCommand.I18N_PREFIX.Banner.YourselfEasterEgg))

                val accentColor = retrievedDiscordUser.accentColor
                if (accentColor != null) {
                    val color = Color(accentColor)
                    color(color.red, color.green, color.blue)
                } else
                    color(114, 137, 218) // TODO: Move this to an object
            }
        }
    }
}