package net.perfectdreams.loritta.commands.images

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
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class SAMExecutor(val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SAMExecutor::class) {
        object Options : CommandOptions() {
            val type = string("type", LocaleKeyData("TODO_FIX_THIS"))
                .choice("1", LocaleKeyData("LOGO_ORIGINAL"))
                .choice("2", LocaleKeyData("LOGO_2"))
                .choice("3", LocaleKeyData("LOGO_3"))
                .register()

            val imageReference = imageReference("image", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val type = args[options.type]
        val imageReference = args[options.imageReference]

        val response = http.post<HttpResponse>("https://gabriela-canary.loritta.website/api/v1/images/sam/$type") {
            body = buildJsonObject {
                putJsonArray("images") {
                    addJsonObject {
                        put("type", "url")
                        put("content", imageReference.url)
                    }
                }
            }.toString()
        }

        println(response.status)

        val result = response.receive<ByteArray>()
        context.sendMessage {
            addFile("sam_logo.png", result)
        }
    }
}