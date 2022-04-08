package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.GigaChadRequest
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.GigaChadCommand

class GigaChadExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(GigaChadExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val averageFanText = string("average_fan_text", GigaChadCommand.I18N_PREFIX.Options.AverageFanText)
                .register()
            val averageEnjoyerText = string("average_enjoyer_text", GigaChadCommand.I18N_PREFIX.Options.AverageEnjoyerText)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val averageFanText = args[options.averageFanText]
        val averageEnjoyerText = args[options.averageEnjoyerText]

        val result = client.handleExceptions(context) {
            client.videos.gigaChad(
                GigaChadRequest(averageFanText, averageEnjoyerText)
            )
        }

        context.sendMessage {
            addFile("gigachad.mp4", result.inputStream())
        }
    }
}