package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GatewayProxyConfig(
    val url: String,
    val authorizationToken: String,
    val replicaId: Int,
)