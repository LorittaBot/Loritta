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

class ManiaTitleCardExecutor(val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ManiaTitleCardExecutor::class) {
        object Options : CommandOptions() {
            val line1 = string("line1", LocaleKeyData("${ManiaTitleCardCommand.LOCALE_PREFIX}.selectLine1"))
                .register()

            val line2 = optionalString("line2", LocaleKeyData("${ManiaTitleCardCommand.LOCALE_PREFIX}.selectLine2"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val line1 = args[options.line1]
        val line2 = args[options.line2]

        val response = http.post<HttpResponse>("https://gabriela-canary.loritta.website/api/v1/images/mania-title-card") {
            body = buildJsonObject {
                putJsonArray("strings") {
                    addJsonObject {
                        put("string", line1)
                    }

                    if (line2 != null) {
                        addJsonObject {
                            put("string", line2)
                        }
                    }
                }
            }.toString()
        }

        println(response.status)

        val result = response.receive<ByteArray>()
        context.sendMessage {
            addFile("mania_title_card.png", result)
        }
    }
}