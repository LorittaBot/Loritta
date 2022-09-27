package net.perfectdreams.loritta.morenitta.listeners

import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class GatewayEventRelayerListener(val m: LorittaBot) : ListenerAdapter() {
    override fun onRawGateway(event: RawGatewayEvent) {
        val gateway = m.lorittaShards.gatewayManager.gateways[event.jda.shardInfo.shardId] ?: error("Missing JDAProxiedKordGateway instance for ${event.jda.shardInfo.shardId}!")
        val kordEvent = KordDiscordEventUtils.parseEventFromString(event.`package`.toString())
        if (kordEvent != null)
            gateway.events.tryEmit(kordEvent)
    }
}