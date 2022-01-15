package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TerminatorAnimeRequest
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class TerminatorAnimeExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(TerminatorAnimeExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val line1 = string("terminator", TerminatorAnimeCommand.I18N_PREFIX.Options.TextTerminator)
                .register()

            val line2 = string("girl", TerminatorAnimeCommand.I18N_PREFIX.Options.TextGirl)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val result = client.handleExceptions(context) {
            client.images.terminatorAnime(
                TerminatorAnimeRequest(line1, line2)
            )
        }

        context.sendMessage {
            addFile("terminator_anime.png", result.inputStream())
        }
    }
}