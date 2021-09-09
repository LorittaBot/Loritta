package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes

class TextLowercaseExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextLowercaseExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Lowercase.Description)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.text]

        context.sendReply(
            content = text.lowercase(),
            prefix = "✍"
        )
    }
}