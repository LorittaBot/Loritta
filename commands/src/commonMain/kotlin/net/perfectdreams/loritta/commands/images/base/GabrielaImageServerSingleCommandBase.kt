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

open class GabrielaImageServerSingleCommandBase(
    val http: HttpClient,
    val endpoint: String,
    val fileName: String
) : CommandExecutor() {
    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val imageReference = args[SingleImageOptions.imageReference]

        val response = http.post<HttpResponse>("https://gabriela-canary.loritta.website$endpoint") {
            body = buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference.url)
                    }
                }
            }.toString()
        }

        val result = response.receive<ByteArray>()
        context.sendMessage {
            addFile(fileName, result)
        }
    }
}