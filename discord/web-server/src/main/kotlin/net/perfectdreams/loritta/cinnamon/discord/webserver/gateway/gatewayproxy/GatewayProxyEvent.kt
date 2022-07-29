package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.gatewayproxy

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GatewayProxyEvent(
    val shardId: Int,
    val event: JsonObject
)