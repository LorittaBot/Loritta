package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.text.VaporwaveUtils

class TextVaporQualityExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextVaporQualityExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", LocaleKeyData("commands.command.vaporwave.options.text"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.text]

        // TODO: Fix Escape Mentions
        val vaporquality = VaporwaveUtils.vaporwave(text.toUpperCase().toCharArray().joinToString(" "))
        context.sendReply(vaporquality, "✍")
    }
}