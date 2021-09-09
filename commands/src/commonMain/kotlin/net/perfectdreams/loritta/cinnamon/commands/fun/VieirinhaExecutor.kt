package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.images.URLImageReference

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