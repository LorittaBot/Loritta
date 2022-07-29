package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext

abstract class ProcessDiscordEventsModule {
    abstract suspend fun processEvent(context: GatewayEventContext): ModuleResult
}