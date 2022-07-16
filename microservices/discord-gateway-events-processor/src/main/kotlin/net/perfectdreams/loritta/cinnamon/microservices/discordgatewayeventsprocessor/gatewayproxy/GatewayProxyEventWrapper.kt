package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy

import kotlinx.datetime.Instant

data class GatewayProxyEventWrapper(
    val receivedAt: Instant,
    val data: GatewayProxyEvent,
) {
    val event by data::event
    val shardId by data::shardId
}