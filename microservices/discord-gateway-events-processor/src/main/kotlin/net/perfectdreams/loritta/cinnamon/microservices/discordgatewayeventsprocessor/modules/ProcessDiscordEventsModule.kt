package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.gateway.Event
import kotlinx.datetime.Instant
import kotlin.reflect.KClass
import kotlin.time.Duration

abstract class ProcessDiscordEventsModule {
    abstract suspend fun processEvent(
        shardId: Int,
        receivedAt: Instant,
        event: Event,
        durations: Map<KClass<*>, Duration>
    ): ModuleResult
}