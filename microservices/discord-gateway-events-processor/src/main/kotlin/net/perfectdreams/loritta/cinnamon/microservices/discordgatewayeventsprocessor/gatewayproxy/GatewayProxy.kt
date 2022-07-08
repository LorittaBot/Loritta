package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.GatewayEvent
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * Receives Discord gateway events via an WebSocket connection
 */
class GatewayProxy(
    val url: String,
    val authorizationToken: String,
    val onMessageReceived: (GatewayEvent) -> (Unit)
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val http = HttpClient {
        install(WebSockets) {
            this.pingInterval = 15_000
        }

        install(HttpTimeout)
    }

    var session: ClientWebSocketSession? = null
    var connectionTries = 1
    val totalEventsReceived = AtomicInteger()
    var state = State.CONNECTING
    var lastEventReceivedAt: Instant? = null
    var connectedAt: Instant? = null

    fun start() {
        coroutineScope.launch {
            while (true) {
                state = State.CONNECTING
                logger.info { "Connecting to Gateway..." }
                connect()
                connectionTries++
                val delay = (1.1.pow(connectionTries.toDouble()) * 50).toLong()
                    .coerceAtMost(60_000)
                logger.info { "Connection closed! Reconnecting in ${delay}ms..." }
                state = State.RECONNECTION_BACKOFF
                delay(delay)
            }
        }
    }

    private suspend fun connect() {
        try {
            val newSession = http.webSocketSession(
                "ws://${url}"
            ) {
                header("Authorization", authorizationToken)
            }

            session = newSession

            for (event in newSession.incoming) {
                when (event) {
                    is Frame.Text -> {
                        val now = Clock.System.now()

                        if (state != State.CONNECTED) {
                            connectionTries = 0 // On a successful connection, reset the try counter
                            connectedAt = now
                        }

                        state = State.CONNECTED
                        totalEventsReceived.addAndGet(1)
                        lastEventReceivedAt = now

                        onMessageReceived.invoke(GatewayEvent(event.data.toString(Charsets.UTF_8)))
                    }
                    is Frame.Binary -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Close -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Ping -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Pong -> {} // No need to handle this / It doesn't seem to be sent to us
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while listening to the session!" }
        }
    }

    override fun close() {
        runBlocking {
            coroutineScope.cancel()
            session?.close()
            http.close()
        }
    }

    enum class State {
        CONNECTING,
        CONNECTED,
        RECONNECTION_BACKOFF
    }
}