package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.tradingviewscraper.TradingViewAPI
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class BrokerTickersUpdater(val config: RootConfig, val services: Pudding, val http: HttpClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // We store the values in a cache before inserting them into a database, because we need all values to update the value in the database!
    val cachedValues = mutableMapOf<String, CachedTickerValues>()

    fun start() {
        val userData = runBlocking {
            http.get("https://br.tradingview.com/chart/") {
                header("Cookie", "sessionid=${config.tradingViewSessionId};")
            }.bodyAsText()
                .lines()
                .first {
                    it.trim().startsWith("var user = ")
                }
                .trim()
                .removePrefix("var user = ")
                .removeSuffix(";")
                .let { Json.parseToJsonElement(it).jsonObject }
        }

        val _tradingApi = TradingViewAPI(userData["auth_token"]!!.jsonPrimitive.content)

        logger.info { "Connecting to TradingView..." }
        _tradingApi.connect()
        logger.info { "Connected! Yay!!" }

        logger.info { "Disabling tickets that aren't in the validStocksCode list! Valid codes: ${LorittaBovespaBrokerUtils.validStocksCodes}" }
        runBlocking {
            services.transaction {
                TickerPrices.update({ TickerPrices.ticker notInList LorittaBovespaBrokerUtils.validStocksCodes }) {
                    it[TickerPrices.enabled] = false
                }
            }
        }

        logger.info { "Starting WebServer..." }

        for (tickerId in LorittaBovespaBrokerUtils.validStocksCodes) {
            logger.info { "Registering ticker $tickerId..."}
            runBlocking {
                // You need to register the update callback BEFORE registering the ticker, because if you register AFTER you may lose some important data!
                _tradingApi.onTickerUpdate(tickerId) { response ->
                    GlobalScope.launch {
                        logger.info { "Ticker $tickerId received an update! $response" }

                        // val tickerShortName = response["short_name"]?.jsonPrimitive?.contentOrNull
                        val currentPrice = response["lp"]?.jsonPrimitive?.doubleOrNull
                        val dailyPriceVariation = response["chp"]?.jsonPrimitive?.doubleOrNull
                        val currentSession = response["current_session"]?.jsonPrimitive?.contentOrNull

                        val currentCachedValue = cachedValues[tickerId] ?: CachedTickerValues(
                            null,
                            null,
                            null
                        )

                        val newCachedValue = currentCachedValue.copy(
                            value = currentPrice,
                            dailyPriceVariation = dailyPriceVariation,
                            currentSession = currentSession
                        )

                        cachedValues[tickerId] = newCachedValue
                        val (cachedCurrentPrice, cachedDailyPriceVariation, cachedCurrentSession) = newCachedValue

                        if (cachedCurrentPrice != null && cachedDailyPriceVariation != null && cachedCurrentSession != null) {
                            // If all cached values are present, we will update it in our database!
                            logger.info { "Updating $tickerId values to $newCachedValue" }
                            val priceInSonhos = (cachedCurrentPrice * 100).toLong()

                            services.transaction {
                                TickerPrices.upsert(TickerPrices.ticker) {
                                    it[TickerPrices.ticker] = tickerId
                                    it[TickerPrices.value] = priceInSonhos
                                    it[TickerPrices.dailyPriceVariation] = cachedDailyPriceVariation
                                    it[TickerPrices.status] = cachedCurrentSession

                                    it[TickerPrices.lastUpdatedAt] = Instant.now()
                                }
                            }
                        } else {
                            logger.info { "Not updating $tickerId values because not all required parameters are present... yet! $newCachedValue" }
                        }
                    }
                }

                _tradingApi.registerTicker(tickerId)
            }
        }

        logger.info { "Up and running! Stonks!!" }
        Thread.sleep(Long.MAX_VALUE)
    }
}