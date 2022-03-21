package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class VieirinhaExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(VieirinhaExecutor::class) {
        object Options : ApplicationCommandOptions() {
            // Unused because... well, we don't need it :P
            val question = string("question", VieirinhaCommand.I18N_PREFIX.Options.Question)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            embed {
                author("Vieirinha", null, "http://i.imgur.com/rRtHdti.png")

                description = context.i18nContext.get(
                    VieirinhaCommand.I18N_PREFIX.Responses(
                        VieirinhaCommand.PUNCTUATIONS.random()
                    )
                ).random()
            }
        }
    }
}