package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class TextUppercaseExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {

        inner class Options : LocalizedApplicationCommandOptions(loritta) {
            val text = string("text", TextTransformCommand.I18N_PREFIX.Uppercase.Description)
                
        }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]

        context.sendReply(
            content = text.uppercase(),
            prefix = "✍"
        )
    }
}