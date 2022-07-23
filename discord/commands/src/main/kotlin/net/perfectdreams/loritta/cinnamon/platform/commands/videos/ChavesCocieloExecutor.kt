package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CocieloChavesRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class ChavesCocieloExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            // The description is replaced with "User, URL or Emote" so we don't really care that we are using "TodoFixThisData" here
            val friend1Image = imageReference("friend1", TodoFixThisData)
                .register()
            val friend2Image = imageReference("friend2", TodoFixThisData)
                .register()
            val friend3Image = imageReference("friend3", TodoFixThisData)
                .register()
            val friend4Image = imageReference("friend4", TodoFixThisData)
                .register()
            val friend5Image = imageReference("friend5", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val friend1 = args[options.friend1Image]
        val friend2 = args[options.friend2Image]
        val friend3 = args[options.friend3Image]
        val friend4 = args[options.friend4Image]
        val friend5 = args[options.friend5Image]

        val result = client.handleExceptions(context) {
            client.videos.cocieloChaves(
                CocieloChavesRequest(
                    URLImageData(
                        friend1.url
                    ),
                    URLImageData(
                        friend2.url
                    ),
                    URLImageData(
                        friend3.url
                    ),
                    URLImageData(
                        friend4.url
                    ),
                    URLImageData(
                        friend5.url
                    )
                )
            )
        }

        context.sendMessage {
            addFile("cocielo_chaves.mp4", result.inputStream())
        }
    }
}