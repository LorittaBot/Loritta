package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy

import dev.kord.gateway.Command
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

/**
 * Receives Discord gateway events via an WebSocket connection
 */
class GatewayProxy(
    val url: String,
    val authorizationToken: String,
    val onMessageReceived: (GatewayProxyEvent) -> (Unit)
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val http = HttpClient(CIO) {
        install(WebSockets) {
            this.pingInterval = 5_000
        }
    }

    var session: DefaultClientWebSocketSession? = null
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
                lastEventReceivedAt = null

                delay(delay)
            }
        }
    }

    private suspend fun connect() {
        // This is a hacky workaround, because for some reason our connection gets removed from Loritta Legacy's WebSocket list
        val job = coroutineScope.launch {
            while (true) {
                if (state == State.CONNECTED) {
                    val lastEventReceivedAt = lastEventReceivedAt
                    if (lastEventReceivedAt != null && Clock.System.now() - lastEventReceivedAt > 60.seconds) {
                        logger.warn { "We haven't received an event on the connection $url for longer than 60s! We will close the connection and restart..." }
                        session?.close(
                            CloseReason(
                                CloseReason.Codes.NORMAL,
                                "Haven't received an event for longer than 60s, so we will restart the connection"
                            )
                        )
                        return@launch
                    }
                }

                delay(1_000)
            }
        }

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

                        val receivedEvent = event.data.toString(Charsets.UTF_8)
                        val gwProxyEvent = Json.decodeFromString<GatewayProxyEvent>(receivedEvent)

                        onMessageReceived.invoke(gwProxyEvent)
                    }
                    is Frame.Binary -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Close -> {} // This isn't received by us because it isn't a raw connection
                    is Frame.Ping -> {} // This isn't received by us because it isn't a raw connection
                    is Frame.Pong -> {} // This isn't received by us because it isn't a raw connection
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while listening to the WebSocket session $url!" }
        }

        val closeReason = session?.closeReason?.await()
        logger.warn { "WebSocket session $url seems to have been closed! Close Reason: $closeReason" }
        session = null
        job.cancel()
    }

    /**
     * Sends a gateway event to the connected [session].
     *
     * @param shardId the shard ID
     * @param command the gateway command
     */
    suspend fun send(shardId: Int, command: Command) {
        val session = session
        if (state != State.CONNECTED)
            throw IllegalStateException("Tried sending an event while the connection isn't connected!")

        if (session == null)
            throw IllegalStateException("Session is null, so we can't send events!")

        session.send(
            Json.encodeToString(
                GatewayProxyEvent(
                    shardId,
                    Json.encodeToJsonElement(
                        Command.SerializationStrategy,
                        command
                    ).jsonObject
                )
            )
        )
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