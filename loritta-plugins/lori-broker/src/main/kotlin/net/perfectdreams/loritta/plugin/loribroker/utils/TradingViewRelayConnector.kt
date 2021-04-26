package net.perfectdreams.loritta.plugin.loribroker.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * TradingView Relay server connector
 */
class TradingViewRelayConnector(
        val urlString: String,
        // The TradingView Relay Server sends a ping every 5 seconds so, just to be safe, we will ignore requests if the
        // server haven't sent any new pings after 10s
        val outdatedPingTime: Long = 10_000,
        val outdatedStocksTime: Long = 60_000
) {
    companion object {
        private val client = HttpClient(CIO) {
            install(WebSockets)
        }
        private val logger = KotlinLogging.logger {}
    }

    private val tickerCallbacks = ConcurrentHashMap<String, MutableList<StockCallback>>()
    private var lastStocksPacketReceivedAt = 0L
    private var lastPingPacketReceivedAt = 0L
    private var tickers = ConcurrentHashMap<String, JsonObject?>()
    private var session: ClientWebSocketSession? = null
    private var isActive = false
    private var isClosed = false
    private var atLeastOnePingPacketWasReceived = false

    /**
     * Starts the TradingView Relay client, this will connect to the TradingView Relay Server.
     * If connection is lost, the client will try reconnecting every 1s until it succeeds.
     */
    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            this@TradingViewRelayConnector.isActive = true
            while (this@TradingViewRelayConnector.isActive) {
                try {
                    connect()
                } catch (e: Exception) {
                    logger.warn(e) { "Disconnected due to exception! Trying again in 1s..." }
                }

                atLeastOnePingPacketWasReceived = false
                logger.warn { "Disconnected! Trying again in 1s..." }
                delay(1_000)
            }
        }

        // Keep checking if the ping is "acceptable"
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                if (atLeastOnePingPacketWasReceived) {
                    val lastPingPacketReceivedAt = System.currentTimeMillis() - lastPingPacketReceivedAt

                    if (lastPingPacketReceivedAt >= 300_000) {
                        logger.warn { "Ping was sent more than 300s ago! Closing WebSocket..." }
                        client.close() // Close the connection and reconnect
                    }

                    delay(5_000)
                } else {
                    delay(15_000)
                }
            }
        }
    }

    /**
     * Connects to the TradingView Relay Server
     */
    private suspend fun connect() {
        client.webSocket(urlString) {
            isClosed = false
            session = this
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val packet = frame.readText()

                            val sentAt = packet.substringBefore('-')
                            val content = packet.substringAfter("-")

                            logger.debug { packet }

                            if (content == "pong") {
                                lastPingPacketReceivedAt = sentAt.toLong()
                                atLeastOnePingPacketWasReceived = true
                            } else {
                                val asJson = Json.parseToJsonElement(content).jsonObject
                                val tickerId = asJson["short_name"]!!.jsonPrimitive.content

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
        isClosed = true
    }

    /**
     * Shuts down the currently active TradingView Relay client
     */
    suspend fun shutdown() {
        isActive = false
        session?.close()
    }

    /**
     * Gets ticker data for the [tickerId]
     *
     * @param tickerId the ticker ID
     *
     * @throws DisconnectedRelayException if the client is not connected to the relay
     * @throws PingStocksTimeoutException if the last received packet was sent more than [lastPingPacketReceivedAt]ms ago
     * @throws OutdatedStocksDataException if the last received stocks data was sent more than [lastStocksPacketReceivedAt]ms ago
     *
     * @return the ticker data or null if it wasn't received yet.
     */
    fun getTicker(tickerId: String): JsonObject? {
        return if (tickers.containsKey(tickerId)) {
            val session = session
            if (session == null || isClosed)
                throw DisconnectedRelayException("Can't get $tickerId ticker data because I'm disconnected from the relay server!")

            // We don't need to check for stale data if the ticker doesn't exist
            val diffLastPing = (System.currentTimeMillis() - lastPingPacketReceivedAt)
            if (diffLastPing >= outdatedPingTime)
                throw PingStocksTimeoutException("Ping stocks timeout when trying to get $tickerId ticker! Last ping was received ${diffLastPing}ms ago!")

            val ticketData = tickers[tickerId]
            val diffLastStocks = (System.currentTimeMillis() - lastStocksPacketReceivedAt)

            // The "isStockMarketOpen" check is here to avoid errors when the stock data is *actually* closed, which
            // causes the relay to not send new data until the server is restarted or when the stock market is open again.
            //
            // So, to avoid this, we are going to add some additional checks.
            val currentSession = ticketData?.get("current_session")?.jsonPrimitive?.contentOrNull

            // We have some fail safes, to avoid buggy states and wrecking havoc.
            // First, we will check if the currentSession is "market" (open) and the stock market should *not* be open
            // This is a instant fail
            if (currentSession == LoriBrokerPlugin.MARKET && !isStockMarketOpen())
                throw OutdatedStocksDataException("Outdated stocks data when trying to get $tickerId ticker! Ticker is open to market but we are outside of the stock market open hours! Last stock data was received ${diffLastStocks}ms ago!")

            // The second fail safe is if the market is checked whenever the ticker session is market
            if (currentSession == LoriBrokerPlugin.MARKET) {
                // In this case, we check if the data is not stale and, if it is, we fail
                if (diffLastStocks >= outdatedStocksTime)
                    throw OutdatedStocksDataException("Outdated stocks data when trying to get $tickerId ticker! Last stock data was received ${diffLastStocks}ms ago!")
            }

            // We don't really care about if the session is not market because if it ain't market == can't buy/sell, so the problem would be very small. (as in: won't cause Loritta issues)
            // Also we don't check if != market but stocks should be open, we don't check that because of holidays and stuff like that.
            ticketData
        } else
            null
    }

    /**
     * Checks if the stock market *should* be open at this moment.
     *
     * Brazil's Stock Market is open from 10am to 5pm, not open on the end of week and not open during holidays.
     *
     * **Attention:** This does not check for holidays and stuff!
     *
     * @return if the stock market should be open at this moment
     */
    private fun isStockMarketOpen(): Boolean {
        val now = Instant.now()
                .atZone(ZoneId.of("America/Sao_Paulo"))

        return (now.hour in 10..17 && !(now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY))
    }

    /**
     * Gets or retrieves ticker data for the [tickerId]
     *
     * @param tickerId the ticker ID
     * @see getTicker
     * @return the ticker data
     */
    suspend fun getOrRetrieveTicker(tickerId: String, requiredFields: List<String> = listOf("lp", "description", "current_session")): JsonObject {
        val ticker = getTicker(tickerId)
        if (ticker != null && !(requiredFields.any { field -> !ticker.containsKey(field) }))
            return ticker

        return suspendCoroutine { continuation ->
            onTickerUpdate(tickerId, requiredFields, continuation)
        }
    }

    /**
     * Calls back when a ticker is updated
     *
     * @param tickerId the ticker ID
     * @param requiredFields what fields are required in the ticker data object for this callback be activated
     * @param continuation the coroutine continuation
     */
    fun onTickerUpdate(tickerId: String, requiredFields: List<String>, continuation: Continuation<JsonObject>) {
        tickerCallbacks.getOrPut(tickerId) { mutableListOf() }.apply {
            this.add(StockCallback(requiredFields, continuation))
        }
    }

    private class StockCallback(val requiredFields: List<String>, val continuation: Continuation<JsonObject>)
}