package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.discordinteraktions.common.builder.message.create.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions

class VieirinhaExecutor() : CommandExecutor() {
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