package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class TextClapExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {

        inner class Options : LocalizedApplicationCommandOptions(loritta) {
            val text = string("text", TextTransformCommand.I18N_PREFIX.Clap.Options.Text(TextTransformCommand.CLAP_EMOJI))
                

            val emoji = optionalString("emoji", TextTransformCommand.I18N_PREFIX.Clap.Options.Emoji)
                
        }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]
        val emoji = args[options.emoji] ?: TextTransformCommand.CLAP_EMOJI

        context.sendReply(
            content = "$emoji${text.split(" ").joinToString(emoji)}$emoji",
            prefix = "✍"
        )
    }
}