package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RootConfig(
    val tradingViewSessionId: String,
    val pudding: PuddingConfig
)