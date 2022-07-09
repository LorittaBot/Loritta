package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.gateway.Event

abstract class ProcessDiscordEventsModule {
    abstract suspend fun processEvent(shardId: Int, event: Event): ModuleResult
}