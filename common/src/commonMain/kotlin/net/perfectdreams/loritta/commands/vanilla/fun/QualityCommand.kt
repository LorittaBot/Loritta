package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.`fun`.declarations.TextTransformDeclaration

class QualityCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(TextTransformDeclaration.Quality, TextTransformDeclaration.Root) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.quality"
    }

    override suspend fun executes(context: CommandContext) {
        // TODO: Fix escapeMentions
        val quality = context.optionsManager.getString(TextTransformDeclaration.Options.text).toCharArray().joinToString(" ")
            .toUpperCase()
            // .escapeMentions()

        context.reply(
            LorittaReply(message = quality, prefix = "✍")
        )
    }
}