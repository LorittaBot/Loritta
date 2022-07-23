package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.HelpCommand

class HelpExecutor: SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions()

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            embed {
                title = "${Emotes.LoriHeart} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.LorittaHelp)}"
                description = context.i18nContext.get(HelpCommand.I18N_PREFIX.Intro(mentionUser(context.user))).joinToString("\n\n")

                color = Color(26, 160, 254) // TODO: Move this to a object

                field(
                    "${Emotes.LoriPat} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.CommandList)}",
                    "${context.loritta.config.website}commands"
                )
                field(
                    "${Emotes.LoriHm} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.SupportServer)}",
                    "${context.loritta.config.website}support"
                )
                field(
                    "${Emotes.LoriYay} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.AddMe)}",
                    "${context.loritta.config.website}dashboard"
                )
                field(
                    "${Emotes.LoriRich} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Donate)}",
                    "${context.loritta.config.website}donate"
                )
                field(
                    "${Emotes.LoriReading} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Blog)}",
                    "${context.loritta.config.website}blog"
                )
                field(
                    "${Emotes.LoriRage} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Guidelines)}",
                    "${context.loritta.config.website}guidelines"
                )

                thumbnailUrl = context.loritta.config.website + "assets/img/lori_help_short.png"
            }
        }
    }
}