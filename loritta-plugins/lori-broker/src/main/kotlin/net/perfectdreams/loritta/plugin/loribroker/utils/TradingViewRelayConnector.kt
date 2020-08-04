package net.perfectdreams.loritta.plugin.loribroker.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Connects to a TradingView relay server
 */
class TradingViewRelayConnector(
        val urlString: String,
        val outdatedPingTime: Long = 7_500,
        val outdatedStocksTime: Long = 60_000
) {
    companion object {
        private val client = HttpClient(CIO) {
            install(WebSockets)
        }
        private val logger = KotlinLogging.logger {}
    }
    private val tickerCallbacks = ConcurrentHashMap<String, MutableList<StockCallback>>()
    var lastStocksPacketReceivedAt = 0L
    var lastPingPacketReceivedAt = 0L
    var tickers = ConcurrentHashMap<String, JsonObject?>()

    /**
     * Starts the TradingView Relay client, it will automatically reconnect if it loses connection
     */
    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    connect()
                } catch (e: Exception) { logger.warn(e) { "Disconnected due to exception! Trying again in 1s..." } }
                logger.warn { "Disconnected! Trying again in 1s..." }
                Thread.sleep(1_000)
            }
        }
    }

    var session: ClientWebSocketSession? = null

    suspend fun connect() {
        client.webSocket(urlString) {
            session = this
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val packet = frame.readText()

                            val sentAt = packet.substringBefore('-')
                            val content = packet.substringAfter("-")

                            logger.info { packet }

                            if (content == "pong") {
                                lastPingPacketReceivedAt = sentAt.toLong()
                            } else {
                                val asJson = Json.parseJson(content).jsonObject
                                val tickerId = asJson["short_name"]!!.content

                                tickers[tickerId] = asJson

                                // We set the "last packet received at" after we set the new content to avoid any exceptions that may arise while parsing the JSON
                                // Because maybe invalid JSON can be sent, and setting the lastPacketReceivedAt field before would cause "we received new data but it failed so... yeah"
                                lastStocksPacketReceivedAt = sentAt.toLong()

                                // Callbacks should be called after the lastPacketReceivedAt, since if we call before it will cause exceptions
                                val removeCallbacks = mutableListOf<StockCallback>()
                                tickerCallbacks[tickerId]?.forEach {
                                    val anyKeyMissing = it.requiredFields.any { field -> !asJson.containsKey(field) }

                                    if (!anyKeyMissing) {
                                        it.continuation.resume(asJson)
                                        removeCallbacks += it
                                    } else {
                                        val missingFields = it.requiredFields.filter { field -> !asJson.containsKey(field) }
                                        logger.debug { "Missing fields: $missingFields" }
                                    }
                                }

                                tickerCallbacks[tickerId]?.removeAll(removeCallbacks)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Exception while reading frames" }
            }
        }
    }

    suspend fun shutdown() {
        session?.close()
    }

    fun getTicker(tickerId: String): JsonObject? {
        return if (tickers.containsKey(tickerId)) {
            // We don't need to check for stale data if the ticker doesn't exist
            val diffLastPing = (System.currentTimeMillis() - lastPingPacketReceivedAt)
            if (diffLastPing >= outdatedPingTime)
                throw PingStocksTimeoutException("Ping stocks timeout when trying to get $tickerId ticker!")

            val diffLastStocks = (System.currentTimeMillis() - lastStocksPacketReceivedAt)
            if (diffLastStocks >= outdatedStocksTime)
                throw OutdatedStocksDataException("Outdated stocks data when trying to get $tickerId ticker!")

            tickers[tickerId]
        } else
            null
    }

    suspend fun getOrRetrieveTicker(tickerId: String, requiredFields: List<String> = listOf("lp", "description", "current_session")): JsonObject {
        val ticker = getTicker(tickerId)
        if (ticker != null && !(requiredFields.any { field -> !ticker.containsKey(field) }))
            return ticker

        return suspendCoroutine { continuation ->
            onTickerUpdate(tickerId, requiredFields, continuation)
        }
    }

    fun onTickerUpdate(tickerId: String, requiredFields: List<String>, continuation: Continuation<JsonObject>) {
        tickerCallbacks.getOrPut(tickerId) { mutableListOf() }.apply {
            this.add(StockCallback(requiredFields, continuation))
        }
    }

    private class StockCallback(val requiredFields: List<String>, val continuation: Continuation<JsonObject>)
}