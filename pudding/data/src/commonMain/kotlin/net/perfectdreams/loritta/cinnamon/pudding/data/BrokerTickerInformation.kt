package net.perfectdreams.loritta.cinnamon.pudding.data

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