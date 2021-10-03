package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SingleImageRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions

open class GabrielaImageServerSingleCommandBase(
    val client: GabrielaImageServerClient,
    val block: suspend GabrielaImageServerClient.(SingleImageRequest) -> (ByteArray),
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[SingleImageOptions.imageReference]

        val result = client.handleExceptions(context) {
            block.invoke(
                client,
                SingleImageRequest(
                    URLImageData(
                        imageReference.url
                    )
                )
            )
        }

        context.sendMessage {
            addFile(fileName, result.inputStream())
        }
    }
}