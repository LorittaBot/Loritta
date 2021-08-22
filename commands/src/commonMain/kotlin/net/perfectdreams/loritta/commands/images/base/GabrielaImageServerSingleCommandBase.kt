package net.perfectdreams.loritta.commands.images.base

import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.executeAndHandleExceptions

open class GabrielaImageServerSingleCommandBase(
    val emotes: Emotes,
    val client: GabrielaImageServerClient,
    val endpoint: String,
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferMessage(false) // Defer message because image manipulation is kinda heavy

        val imageReference = args[SingleImageOptions.imageReference]

        val result = client.executeAndHandleExceptions(
            context,
            emotes,
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
            addFile(fileName, result)
        }
    }
}