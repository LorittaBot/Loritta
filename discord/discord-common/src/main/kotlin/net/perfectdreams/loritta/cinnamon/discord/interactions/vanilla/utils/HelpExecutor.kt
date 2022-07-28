package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.HelpCommand

class HelpExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            embed {
                title = "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.LorittaHelp)}"
                description = context.i18nContext.get(HelpCommand.I18N_PREFIX.Intro(mentionUser(context.user))).joinToString("\n\n")

                color = Color(26, 160, 254) // TODO: Move this to a object

                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriPat} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.CommandList)}",
                    "${context.loritta.config.website}commands"
                )
                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHm} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.SupportServer)}",
                    "${context.loritta.config.website}support"
                )
                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.AddMe)}",
                    "${context.loritta.config.website}dashboard"
                )
                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriRich} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Donate)}",
                    "${context.loritta.config.website}donate"
                )
                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriReading} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Blog)}",
                    "${context.loritta.config.website}blog"
                )
                field(
                    "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriRage} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Guidelines)}",
                    "${context.loritta.config.website}guidelines"
                )

                thumbnailUrl = context.loritta.config.website + "assets/img/lori_help_short.png"
            }
        }
    }
}