package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GatewayProxyEvent(
    val shardId: Int,
    val event: JsonObject
)