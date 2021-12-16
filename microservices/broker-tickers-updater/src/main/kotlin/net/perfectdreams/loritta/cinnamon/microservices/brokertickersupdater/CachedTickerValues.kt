package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater

data class CachedTickerValues(
    val value: Double?,
    val dailyPriceVariation: Double?,
    val currentSession: String?
)