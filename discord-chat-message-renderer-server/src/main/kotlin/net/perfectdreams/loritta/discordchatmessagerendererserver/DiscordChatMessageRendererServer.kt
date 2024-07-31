package net.perfectdreams.loritta.discordchatmessagerendererserver

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.time.ZoneId

class DiscordChatMessageRendererServer {
    private val logger = KotlinLogging.logger {}
    private val rendererManager = DiscordMessageRendererManager(
        ZoneId.of("America/Sao_Paulo"),
        setOf(
            "image/gif",
            "image/jpeg",
            "image/bmp",
            "image/png"
        )
    )
    private var successfulRenders = 0
    private var failedRenders = 0

    fun start() {
        val http = embeddedServer(Netty, port = 8080) {
            routing {
                post("/generate-message") {
                    val body = call.receiveText()

                    val savedMessage = Json.decodeFromString<SavedMessage>(body)

                    try {
                        val image = rendererManager.renderMessage(savedMessage, null)

                        call.respondBytes(
                            image,
                            ContentType.Image.PNG
                        )

                        successfulRenders++
                        logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to render message ${savedMessage.id}!" }
                        call.respondText(
                            e.stackTraceToString(),
                            status = HttpStatusCode.InternalServerError
                        )
                        failedRenders++
                        logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                    }
                }
            }
        }
        http.start(true)
    }
}