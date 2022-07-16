package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.GatewayProxyEventContext

abstract class ProcessDiscordEventsModule {
    abstract suspend fun processEvent(context: GatewayProxyEventContext): ModuleResult
}