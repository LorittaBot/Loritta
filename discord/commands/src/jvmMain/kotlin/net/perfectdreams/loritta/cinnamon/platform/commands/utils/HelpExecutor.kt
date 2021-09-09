package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.create.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class HelpExecutor(): CommandExecutor() {
    companion object : CommandExecutorDeclaration(HelpExecutor::class) {
        object Options : CommandOptions()

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage {
            embed {
                title = "${Emotes.loriHeart} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.LorittaHelp)}"
                description = context.i18nContext.get(HelpCommand.I18N_PREFIX.Intro(mentionUser(context.user))).joinToString("\n\n")

                color = Color(26, 160, 254) // TODO: Move this to a object

                field(
                    "${Emotes.loriPat} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.CommandList)}",
                    "${context.loritta.config.website}commands"
                )
                field(
                    "${Emotes.loriHm} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.SupportServer)}",
                    "${context.loritta.config.website}support"
                )
                field(
                    "${Emotes.loriYay} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.AddMe)}",
                    "${context.loritta.config.website}dashboard"
                )
                field(
                    "${Emotes.loriRich} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Donate)}",
                    "${context.loritta.config.website}donate"
                )
                field(
                    "${Emotes.loriReading} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Blog)}",
                    "${context.loritta.config.website}blog"
                )
                field(
                    "${Emotes.loriRage} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Guidelines)}",
                    "${context.loritta.config.website}guidelines"
                )

                thumbnailUrl = context.loritta.config.website + "assets/img/lori_help_short.png"
            }
        }
    }
}