package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.commands.images.declarations.CortesFlowCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class CortesFlowExecutor(val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CortesFlowExecutor::class) {
        object Options : CommandOptions() {
            val type = string("thumbnail", LocaleKeyData("${CortesFlowCommand.LOCALE_PREFIX}.selectThumbnail"))
                .also { option ->
                    CortesFlowCommand.lists.forEach {
                        option.choice(it, LocaleKeyData(it))
                    }
                }
                .register()

            val text = string("text", LocaleKeyData("${CortesFlowCommand.LOCALE_PREFIX}.selectText"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val type = args[options.type]
        val text = args[options.text]

        val response = http.post<HttpResponse>("https://gabriela.loritta.website/api/v1/images/cortes-flow/$type") {
            body = buildJsonObject {
                putJsonArray("strings") {
                    addJsonObject {
                        put("string", text)
                    }
                }
            }.toString()
        }

        // TODO: Check if this needs to be kept, the types are not validated in the client side
        /* if (response.status == HttpStatusCode.NotFound)
            fail(locale["commands.command.cortesflow.unknownType", serverConfig.commandPrefix]) */

        val cortesFlow = response.receive<ByteArray>()
        context.sendMessage {
            addFile("cortes_flow.jpg", cortesFlow)
        }
    }
}