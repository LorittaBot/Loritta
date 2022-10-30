package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class HelpExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            embed {
                title = "${Emotes.LoriHeart} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.LorittaHelp)}"
                description = context.i18nContext.get(HelpCommand.I18N_PREFIX.Intro(mentionUser(context.user)))
                    .joinToString("\n\n")

                color = Color(26, 160, 254) // TODO: Move this to a object

                field(
                    "${Emotes.LoriPat} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.CommandList)}",
                    "${context.loritta.config.loritta.website.url}commands"
                )
                field(
                    "${Emotes.LoriHm} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.SupportServer)}",
                    "${context.loritta.config.loritta.website.url}support"
                )
                field(
                    "${Emotes.LoriYay} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.AddMe)}",
                    "${context.loritta.config.loritta.website.url}dashboard"
                )
                field(
                    "${Emotes.LoriRich} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Donate)}",
                    "${context.loritta.config.loritta.website.url}donate"
                )
                field(
                    "${Emotes.LoriReading} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Blog)}",
                    "${context.loritta.config.loritta.website.url}blog"
                )
                field(
                    "${Emotes.LoriRage} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Guidelines)}",
                    "${context.loritta.config.loritta.website.url}guidelines"
                )

                thumbnailUrl = context.loritta.config.loritta.website.url + "assets/img/lori_help_short.png"
            }
        }
    }
}