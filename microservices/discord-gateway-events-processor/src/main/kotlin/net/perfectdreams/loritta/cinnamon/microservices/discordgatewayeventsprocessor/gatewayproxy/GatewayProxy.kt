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
            this.pingInterval = 5_000
        }

        install(HttpTimeout)
    }

    var session: ClientWebSocketSession? = null
    var connectionTries = 1
    val totalEventsReceived = AtomicInteger()
    var state = State.CONNECTING
    var lastEventReceivedAt: Instant? = null
    var lastConnection: Instant? = null
    var lastDisconnection: Instant? = null

    fun start() {
        coroutineScope.launch {
            while (true) {
                state = State.CONNECTING
                logger.info { "Connecting to Gateway endpoint $url..." }

                connect()
                connectionTries++

                val delay = (1.1.pow(connectionTries.toDouble()) * 50).toLong()
                    .coerceAtMost(60_000)
                logger.info { "Gateway $url connection closed! Reconnecting in ${delay}ms..." }

                state = State.RECONNECTION_BACKOFF
                lastDisconnection = Clock.System.now()

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
                            logger.info { "Successfully connected to gateway endpoint $url!" }
                            connectionTries = 0 // On a successful connection, reset the try counter
                            lastConnection = now
                            state = State.CONNECTED
                        }

                        totalEventsReceived.addAndGet(1)
                        lastEventReceivedAt = now

                        onMessageReceived.invoke(GatewayEvent(event.data.toString(Charsets.UTF_8)))
                    }
                    is Frame.Binary -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Close -> {
                        logger.info { "WebSocket connection $url has been closed! Reason: ${event.readReason()}" }
                    }
                    is Frame.Ping -> {
                        logger.info { "Received WebSocket ping on connection $url!" }
                    }
                    is Frame.Pong -> {
                        logger.info { "Received WebSocket pong on connection $url!" }
                    }
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while listening to the WebSocket session $url!" }
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