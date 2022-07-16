package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.gateway.Event
import kotlinx.datetime.Instant
import kotlin.reflect.KClass
import kotlin.time.Duration

data class GatewayProxyEventContext(
    val eventType: String,
    val event: Event?,
    val shardId: Int,
    val receivedAt: Instant
) {
    val durations = mutableMapOf<KClass<*>, Duration>()
}