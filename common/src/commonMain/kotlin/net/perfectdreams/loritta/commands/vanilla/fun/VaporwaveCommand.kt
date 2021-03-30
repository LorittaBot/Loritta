package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.utils.text.VaporwaveUtils

class VaporwaveCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(TextTransformDeclaration.Vaporwave, TextTransformDeclaration.Root) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.vaporwave"
    }

    override suspend fun executes(context: CommandContext) {
        // TODO: Fix escapeMentions
        val vaporwave = VaporwaveUtils.vaporwave(context.optionsManager.getString(TextTransformDeclaration.Options.text))
            // .escapeMentions()

        context.reply(
            LorittaReply(message = vaporwave, prefix = "✍")
        )
    }
}