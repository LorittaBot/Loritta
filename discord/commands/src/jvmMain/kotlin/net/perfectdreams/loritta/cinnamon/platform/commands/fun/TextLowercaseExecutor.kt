package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

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