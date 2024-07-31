package net.perfectdreams.loritta.discordchatmessagerendererserver

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.time.ZoneId

class DiscordChatMessageRendererServer {
    private val rendererManager = DiscordMessageRendererManager(
        ZoneId.of("America/Sao_Paulo"),
        setOf(
            "image/gif",
            "image/jpeg",
            "image/bmp",
            "image/png"
        )
    )

    fun start() {
        val http = embeddedServer(Netty, port = 8080) {
            routing {
                post("/generate-message") {
                    val body = call.receiveText()

                    val savedMessage = Json.decodeFromString<SavedMessage>(body)
                    val image = rendererManager.renderMessage(savedMessage, null)

                    call.respondBytes(
                        image,
                        ContentType.Image.PNG
                    )
                }
            }
        }
        http.start(true)
    }
}