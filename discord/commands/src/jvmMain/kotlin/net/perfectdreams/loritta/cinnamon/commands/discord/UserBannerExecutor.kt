package net.perfectdreams.loritta.cinnamon.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.create.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class UserBannerExecutor(val emotes: Emotes, val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(UserBannerExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", UserCommand.I18N_PREFIX.Banner.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[Options.user] ?: context.user

        // We need to retrieve from Discord's API to get the banner info
        // Also, this is the reason why this command is in the Discord InteraKTions platform
        val retrievedDiscordUser = rest.user.getUser(user.id)

        val bannerId = retrievedDiscordUser.banner ?: context.failEphemerally {
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

                image = bannerUrl

                // Easter Egg: Looking up yourself
                if (context.user.id == user.id)
                    footer(context.i18nContext.get(UserCommand.I18N_PREFIX.Banner.YourselfEasterEgg))

                val accentColor = retrievedDiscordUser.accentColor
                if (accentColor != null) {
                    color = Color(accentColor)
                } else
                    color = Color(114, 137, 218) // TODO: Move this to an object
            }
        }
    }
}