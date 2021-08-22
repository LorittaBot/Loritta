package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.images.URLImageReference

class VieirinhaExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(VieirinhaExecutor::class) {
        object Options : CommandOptions() {
            // Unused because... well, we don't need it :P
            val question = string("question", VieirinhaCommand.I18N_PREFIX.Options.Question)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage {
            impersonation("Vieirinha", URLImageReference("http://i.imgur.com/rRtHdti.png"))

            content = context.i18nContext.get(
                VieirinhaCommand.I18N_PREFIX.Responses(
                    VieirinhaCommand.PUNCTUATIONS.random()
                )
            ).random()
        }
    }
}