package net.perfectdreams.loritta.morenitta.listeners

import net.dv8tion.jda.api.events.PreProcessedRawGatewayEvent
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class GatewayEventRelayerListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val EVENTS_TO_BE_RELAYED = setOf(
            "CHANNEL_CREATE",
            "MESSAGE_CREATE",
            "MESSAGE_UPDATE",
            "INVITE_CREATE",
            "INVITE_DELETE",
            "MESSAGE_REACTION_ADD",
            "MESSAGE_REACTION_REMOVE",
            "MESSAGE_REACTION_REMOVE_EMOJI",
            "MESSAGE_REACTION_REMOVE_ALL",
            "INTERACTION_CREATE"
        )
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
}