package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.executeAndHandleExceptions
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor

open class GabrielaImageServerSingleCommandBase(
    val client: GabrielaImageServerClient,
    val endpoint: String,
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[SingleImageOptions.imageReference]

        val result = client.executeAndHandleExceptions(
            context,
                    endpoint,
            buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference.url)
                    }
                }
            }
        )

        context.sendMessage {
            addFile(fileName, result.inputStream())
        }
    }
}