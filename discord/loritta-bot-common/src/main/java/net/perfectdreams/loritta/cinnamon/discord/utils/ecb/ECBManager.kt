package net.perfectdreams.loritta.cinnamon.discord.utils.ecb

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class ECBManager {
    private var updatedAt = 0L
    private var job: Deferred<Map<String, Double>>? = null

    @OptIn(ExperimentalTime::class)
    fun getOrUpdateExchangeRates(): Deferred<Map<String, Double>> {
        val diff = System.currentTimeMillis() - updatedAt

        if (diff >= Duration.hours(1).inWholeMilliseconds) {
            job = GlobalScope.async {
                val jsoup = Jsoup.connect("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml?${System.currentTimeMillis()}")
                    .get()

                val exchangeRates = jsoup.select("Cube").filter { it.hasAttr("currency") }
                    .map { it.attr("currency") to it.attr("rate").toDouble() }
                    .toMap()
                    .toMutableMap()

                exchangeRates["EUR"] = 1.0

                updatedAt = System.currentTimeMillis()

                exchangeRates
            }
        }

        return job!!
    }
}