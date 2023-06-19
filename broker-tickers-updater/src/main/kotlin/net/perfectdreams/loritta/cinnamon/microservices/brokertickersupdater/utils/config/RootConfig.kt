package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RootConfig(
    val tradingViewSessionId: String,
    val pudding: PuddingConfig
)