package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot

class UserBannerExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", UserCommand.I18N_PREFIX.Banner.Options.User)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.user

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
        val bannerUrl =
            "https://cdn.discordapp.com/banners/${user.id.value}/${retrievedDiscordUser.banner}.$extension?size=512"

        context.sendMessage {
            embed {
                title = "\uD83D\uDDBC ${user.username}"

                image = bannerUrl

                // Easter Egg: Looking up yourself
                if (context.user.id == user.id)
                    footer(context.i18nContext.get(UserCommand.I18N_PREFIX.Banner.YourselfEasterEgg))

                val accentColor = retrievedDiscordUser.accentColor
                color = if (accentColor != null) {
                    Color(accentColor)
                } else
                    LorittaColors.DiscordBlurple.toKordColor()
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