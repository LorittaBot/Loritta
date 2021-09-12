package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.utils.text.VaporwaveUtils

class TextVaporwaveExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextVaporwaveExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Vaporwave.Description)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val text = args[options.text]

        // TODO: Fix Escape Mentions
        val vaporwave = VaporwaveUtils.vaporwave(text)
        context.sendReply(
            content = vaporwave,
            prefix = "✍"
        )
    }
}