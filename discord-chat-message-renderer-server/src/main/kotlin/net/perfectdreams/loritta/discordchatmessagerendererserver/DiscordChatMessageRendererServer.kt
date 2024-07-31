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
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.measureTimedValue

class DiscordChatMessageRendererServer {
    private val logger = KotlinLogging.logger {}
    private val rendererManagers = (0 until 4).map {
        DiscordMessageRendererManager(
            ZoneId.of("America/Sao_Paulo"),
            setOf(
                "image/gif",
                "image/jpeg",
                "image/bmp",
                "image/png"
            )
        )
    }
    private val availableRenderers = LinkedBlockingQueue<DiscordMessageRendererManager>()
    private var successfulRenders = 0
    private var failedRenders = 0

    fun start() {
        logger.info { "Using ${rendererManagers.size} renderers" }
        for (rendererManager in rendererManagers) {
            availableRenderers.put(rendererManager)
        }

        val http = embeddedServer(Netty, port = 8080) {
            routing {
                post("/generate-message") {
                    val body = call.receiveText()

                    val savedMessage = Json.decodeFromString<SavedMessage>(body)

                    logger.info { "Attempting to get a available renderer... Available renderers: ${availableRenderers.size}/${rendererManagers.size}" }

                    val rendererManager = measureTimedValue { availableRenderers.take() }.also {
                        logger.info { "Took ${it.duration} to get an available renderer for ${savedMessage.id}" }
                    }.value

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
                    } finally {
                        logger.info { "Putting $rendererManager back into the available renderers queue" }
                        availableRenderers.add(rendererManager)
                    }
                }
            }
        }
        http.start(true)
    }
}