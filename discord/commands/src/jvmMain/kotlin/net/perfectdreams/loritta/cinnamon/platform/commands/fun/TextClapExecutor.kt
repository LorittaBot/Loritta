package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class TextClapExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(TextClapExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Clap.Options.Text(TextTransformDeclaration.CLAP_EMOJI))
                .register()

            val emoji = optionalString("emoji", TextTransformDeclaration.I18N_PREFIX.Clap.Options.Emoji)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]
        val emoji = args[options.emoji] ?: TextTransformDeclaration.CLAP_EMOJI

        context.sendReply(
            content = "$emoji${text.split(" ").joinToString(emoji)}$emoji",
            prefix = "✍"
        )
    }
}