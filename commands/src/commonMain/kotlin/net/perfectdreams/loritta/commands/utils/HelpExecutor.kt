package net.perfectdreams.loritta.commands.utils

import net.perfectdreams.loritta.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes

class HelpExecutor(val emotes: Emotes): CommandExecutor() {
    companion object : CommandExecutorDeclaration(HelpExecutor::class) {
        object Options : CommandOptions()

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendEmbed {
            title = "${emotes.loriHeart} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.LorittaHelp)}"
            description = context.i18nContext.get(HelpCommand.I18N_PREFIX.Intro(context.user.asMention)).joinToString("\n\n")

            color(26, 160, 254) // TODO: Move this to a object

            field("${emotes.loriPat} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.CommandList)}", "${context.loritta.config.website}commands")
            field("${emotes.loriHm} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.SupportServer)}", "${context.loritta.config.website}support")
            field("${emotes.loriYay} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.AddMe)}", "${context.loritta.config.website}dashboard")
            field("${emotes.loriRich} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Donate)}", "${context.loritta.config.website}donate")
            field("${emotes.loriReading} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Blog)}", "${context.loritta.config.website}blog")
            field("${emotes.loriRage} ${context.i18nContext.get(HelpCommand.I18N_PREFIX.Guidelines)}", "${context.loritta.config.website}guidelines")

            thumbnail(context.loritta.config.website + "assets/img/lori_help_short.png")
        }
    }
}