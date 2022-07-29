package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.gateway.Event
import kotlinx.datetime.Instant
import kotlin.reflect.KClass
import kotlin.time.Duration

data class GatewayEventContext(
    val event: Event?,
    val shardId: Int,
    val receivedAt: Instant
) {
    val durations = mutableMapOf<KClass<*>, Duration>()
}