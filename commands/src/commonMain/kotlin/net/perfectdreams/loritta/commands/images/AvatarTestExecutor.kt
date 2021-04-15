package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.commands.images.declarations.ManiaTitleCardCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class AvatarTestExecutor(val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AvatarTestExecutor::class) {
        object Options : CommandOptions() {
            val imageReference = imageReference("image_ref_test", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val imageReference = args[options.imageReference]

        val response = http.post<HttpResponse>("https://gabriela-canary.loritta.website/api/v1/images/canella-dvd") {
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
            addFile("canella_dvd.png", result)
        }
    }
}