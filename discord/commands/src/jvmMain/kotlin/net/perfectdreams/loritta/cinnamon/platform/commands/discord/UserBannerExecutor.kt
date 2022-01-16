package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class UserBannerExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(UserBannerExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val user = optionalUser("user", UserCommand.I18N_PREFIX.Banner.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[Options.user] ?: context.user

        // We need to retrieve from Discord's API to get the banner info
        // Also, this is the reason why this command is in the Discord InteraKTions platform
        val retrievedDiscordUser = rest.user.getUser(user.id)

        val bannerId = retrievedDiscordUser.banner ?: context.failEphemerally {
            styled(
                context.i18nContext.get(
                    I18nKeysData.Commands.Command.User.Banner.UserDoesNotHaveAnBanner(mentionUser(user, false))
                ),
                prefix = Emotes.Error
            )
        }

        val extension = if (bannerId.startsWith("a_")) "gif" else "png"
        val bannerUrl = "https://cdn.discordapp.com/banners/${user.id.value}/${retrievedDiscordUser.banner}.$extension?size=512"

        context.sendMessage {
            embed {
                title = "\uD83D\uDDBC ${user.name}"

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

            actionRow {
                linkButton(
                    url = bannerUrl
                ) {
                    label = context.i18nContext.get(UserCommand.I18N_PREFIX.Banner.OpenBannerInBrowser)
                }
            }
        }
    }
}