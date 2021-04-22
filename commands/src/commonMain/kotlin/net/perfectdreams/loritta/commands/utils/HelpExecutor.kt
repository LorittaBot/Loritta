package net.perfectdreams.loritta.commands.utils

import net.perfectdreams.loritta.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.embed.LorittaColor

class HelpExecutor(val emotes: Emotes): CommandExecutor() {
    companion object : CommandExecutorDeclaration(HelpExecutor::class) {
        object Options : CommandOptions()

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendEmbed {
            body {
                title = "${emotes.loriHeart} ${context.locale["${HelpCommand.LOCALE_PREFIX}.lorittaHelp"]}"
                description = context.locale.getList("${HelpCommand.LOCALE_PREFIX}.intro")
                    .joinToString("\n\n", transform = { it.replace("{0}", context.user.asMention) })
                color = LorittaColor.LORITTA_AQUA
            }
            field("${emotes.loriPat} ${context.locale["${HelpCommand.LOCALE_PREFIX}.commandList"]}", "${context.loritta.config.website}commands")
            field("${emotes.loriHm} ${context.locale["${HelpCommand.LOCALE_PREFIX}.supportServer"]}", "${context.loritta.config.website}support")
            field("${emotes.loriYay} ${context.locale["${HelpCommand.LOCALE_PREFIX}.addMe"]}", "${context.loritta.config.website}dashboard")
            field("${emotes.loriYay} ${context.locale["${HelpCommand.LOCALE_PREFIX}.donate"]}", "${context.loritta.config.website}donate")
            field("${emotes.loriYay} ${context.locale["${HelpCommand.LOCALE_PREFIX}.blog"]}", "${context.loritta.config.website}blog")
            field("${emotes.loriYay} ${context.locale["${HelpCommand.LOCALE_PREFIX}.guidelines"]}", "${context.loritta.config.website}guidelines")
            images {
                thumbnail = context.loritta.config.website + "assets/img/lori_help_short.png"
            }
        }
    }
}