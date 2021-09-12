package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor

open class GabrielaImageServerTwoCommandBase(
    val client: GabrielaImageServerClient,
    val endpoint: String,
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference1 = args[TwoImagesOptions.imageReference1]
        val imageReference2 = args[TwoImagesOptions.imageReference2]

        val result = client.executeAndHandleExceptions(
            context,
                    endpoint,
            buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference1.url)
                    }

                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference2.url)
                    }
                }
            }
        )

        context.sendMessage {
            addFile(fileName, result.inputStream())
        }
    }
}