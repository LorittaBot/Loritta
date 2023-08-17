package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BrokerTickerInformation(
    val ticker: String,
    val status: String,
    val value: Long,
    val dailyPriceVariation: Double,
    val lastUpdatedAt: Instant
)