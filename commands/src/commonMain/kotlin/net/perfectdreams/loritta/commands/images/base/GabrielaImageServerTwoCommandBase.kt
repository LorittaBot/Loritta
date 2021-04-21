package net.perfectdreams.loritta.commands.images.base

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.emotes.Emotes

open class GabrielaImageServerTwoCommandBase(
    val emotes: Emotes,
    val http: HttpClient,
    val endpoint: String,
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val imageReference1 = args[TwoImagesOptions.imageReference1]
        val imageReference2 = args[TwoImagesOptions.imageReference2]

        val response = http.post<HttpResponse>("https://gabriela.loritta.website$endpoint") {
            body = buildJsonObject {
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
            }.toString()
        }

        // If the status code is between 400.499, then it means that it was (probably) a invalid input or something
        if (response.status.value in 400..499)
            context.fail(context.locale["commands.noValidImageFound", emotes.loriSob], emotes.loriSob)
        else if (response.status.value !in 200..299) // This should show the error message because it means that the server had a unknown error
            context.fail(context.locale["commands.errorWhileExecutingCommand", emotes.loriRage, emotes.loriSob], "\uD83E\uDD37")

        val result = response.receive<ByteArray>()
        context.sendMessage {
            addFile(fileName, result)
        }
    }
}