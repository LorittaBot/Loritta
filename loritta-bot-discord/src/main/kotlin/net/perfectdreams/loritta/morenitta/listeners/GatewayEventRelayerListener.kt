package net.perfectdreams.loritta.morenitta.listeners

import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.PreProcessedRawGatewayEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.JDAImpl
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.gateway.JDAProxiedKordGateway

class GatewayEventRelayerListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val EVENTS_TO_BE_RELAYED = setOf("INTERACTION_CREATE")
    }
    override fun onPreProcessedRawGateway(event: PreProcessedRawGatewayEvent) {
        // We can't use RawGatewayEvent due to https://github.com/DV8FromTheWorld/JDA/issues/2333
        val type = event.`package`.getString("t", null)
        if (type !in EVENTS_TO_BE_RELAYED)
            return

        val gateway = m.lorittaShards.gatewayManager.gateways[event.jda.shardInfo.shardId] ?: error("Missing JDAProxiedKordGateway instance for ${event.jda.shardInfo.shardId}!")
        val kordEvent = KordDiscordEventUtils.parseEventFromString(event.`package`.toString())
        if (kordEvent != null)
            gateway.events.tryEmit(kordEvent)
    }

    override fun onReady(event: ReadyEvent) {
        val gateway = JDAProxiedKordGateway(event.jda as JDAImpl)
        m.lorittaShards.gatewayManager.proxiedKordGateways[event.jda.shardInfo.shardId] = gateway

        m.scope.launch {
            gateway.installDiscordInteraKTions(m.interaKTions)
        }
    }
}