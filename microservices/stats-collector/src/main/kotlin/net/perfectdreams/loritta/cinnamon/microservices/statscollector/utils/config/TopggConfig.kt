package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class TopggConfig(
    val clientId: Long,
    val token: String
)