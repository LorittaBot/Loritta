package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.common.utils.text.VaporwaveUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class TextVaporQualityExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(TextVaporQualityExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Vaporquality.Description)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]

        // TODO: Fix Escape Mentions
        val vaporquality = VaporwaveUtils.vaporwave(text.toUpperCase().toCharArray().joinToString(" "))
        context.sendReply(vaporquality, "✍")
    }
}