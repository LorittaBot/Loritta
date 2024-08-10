package net.perfectdreams.loritta.discordchatmessagerendererserver

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRenderer
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTimedValue

class DiscordChatMessageRendererServer {
    private val logger = KotlinLogging.logger {}
    private val http = HttpClient {}
    private val messageHtmlRenderer = DiscordMessageRenderer(
        ZoneId.of("America/Sao_Paulo"),
        setOf(
            "image/gif",
            "image/jpeg",
            "image/bmp",
            "image/png"
        )
    )
    private val rendererManagers = (0 until 8).map {
        DiscordMessageRendererManager(messageHtmlRenderer) { this.firefox() }
    }
    private val availableRenderers = CoroutineQueue<DiscordMessageRendererManager>(rendererManagers.size)
    private var successfulRenders = 0
    private var failedRenders = 0
    private val pendingRequests = AtomicInteger()

    fun start() {
        logger.info { "Using ${rendererManagers.size} renderers" }
        for (rendererManager in rendererManagers) {
            runBlocking { availableRenderers.send(rendererManager) }
        }

        val http = embeddedServer(CIO, port = 8080) {
            routing {
                // Dumps all currently running coroutines
                get("/coroutines") {
                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)
                    DebugProbes.dumpCoroutines(ps)
                    call.respondText(os.toString(Charsets.UTF_8))
                }

                post("/generate-message") {
                    val body = call.receiveText()

                    val savedMessage = Json.decodeFromString<SavedMessage>(body)

                    // We will attempt to download all required images before rendering, this way, we don't need to wait all images to individually download on the browser itself
                    // The less time we spend locking a renderer, the better!
                    // The image data will be embedded in the generated HTML
                    // TODO: ^ That's not implemented yet
                    val savedMessageHtmlContent = messageHtmlRenderer.renderMessage(
                        savedMessage,
                        null
                    )

                    logger.info { "Attempting to get a available renderer for message ${savedMessage.id}... Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }

                    try {
                        pendingRequests.incrementAndGet()
                        val rendererManager = measureTimedValue {
                            availableRenderers.receive()
                        }.also {
                            logger.info { "Took ${it.duration} to get an available renderer for ${savedMessage.id}! Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }
                        }.value

                        val image = try {
                            val image = rendererManager.renderMessage(savedMessage, savedMessageHtmlContent)

                            successfulRenders++
                            logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                            image
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to render message ${savedMessage.id}! Request Body: $body" }
                            call.respondText(
                                e.stackTraceToString(),
                                status = HttpStatusCode.InternalServerError
                            )
                            failedRenders++
                            null
                        } finally {
                            logger.info { "Putting $rendererManager back into the available renderers queue" }
                            availableRenderers.send(rendererManager)
                        }

                        if (image != null) {
                            call.respondBytes(
                                image,
                                ContentType.Image.PNG
                            )
                        } else {
                            call.respondText("", status = HttpStatusCode.InternalServerError)
                        }
                    } finally {
                        pendingRequests.decrementAndGet()
                    }
                }
            }
        }
        http.start(true)
    }
}