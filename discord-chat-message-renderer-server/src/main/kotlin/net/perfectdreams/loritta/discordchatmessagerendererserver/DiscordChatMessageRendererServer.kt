package net.perfectdreams.loritta.discordchatmessagerendererserver

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.ZoneId
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
    private val availableRenderers = Channel<DiscordMessageRendererManager>(Channel.UNLIMITED)
    private var successfulRenders = 0
    private var failedRenders = 0
    private val dispatcher = Dispatchers.IO.limitedParallelism(rendererManagers.size)

    fun start() {
        logger.info { "Using ${rendererManagers.size} renderers" }
        for (rendererManager in rendererManagers) {
            availableRenderers.trySend(rendererManager)
        }

        val http = embeddedServer(Netty, port = 8080) {
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

                    logger.info { "Attempting to get a available renderer for message ${savedMessage.id}..." }

                    val rendererManager = measureTimedValue { availableRenderers.receive() }.also {
                        logger.info { "Took ${it.duration} to get an available renderer for ${savedMessage.id}" }
                    }.value

                    try {
                        val image = withContext(dispatcher) { rendererManager.renderMessage(savedMessage, null) }

                        call.respondBytes(
                            image,
                            ContentType.Image.PNG
                        )

                        successfulRenders++
                        logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to render message ${savedMessage.id}! Request Body: $body" }
                        call.respondText(
                            e.stackTraceToString(),
                            status = HttpStatusCode.InternalServerError
                        )
                        failedRenders++
                        logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                    } finally {
                        logger.info { "Putting $rendererManager back into the available renderers queue" }
                        availableRenderers.send(rendererManager)
                    }
                }
            }
        }
        http.start(true)
    }
}