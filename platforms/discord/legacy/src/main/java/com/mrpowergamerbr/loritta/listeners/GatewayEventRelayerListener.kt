package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordGatewayEvents
import org.jetbrains.exposed.sql.insert

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
    companion object {
        private val RELAYED_EVENTS = listOf(
            "CHANNEL_CREATE",
            "CHANNEL_UPDATE",
            "CHANNEL_DELETE",
            "GUILD_MEMBER_ADD",
            "GUILD_MEMBER_REMOVE",
            "GUILD_MEMBER_UPDATE",
            "USER_UPDATE",
            "MESSAGE_REACTION_ADD",
            "MESSAGE_REACTION_REMOVE",
            "MESSAGE_REACTION_REMOVE_EMOJI",
            "MESSAGE_REACTION_REMOVE_ALL"
        )
    }
    override fun onRawGateway(event: RawGatewayEvent) {
        if (event.type !in RELAYED_EVENTS)
            return

        GlobalScope.launch(m.coroutineDispatcher) {
            m.pudding.transaction {
                DiscordGatewayEvents.insert {
                    it[DiscordGatewayEvents.type] = event.type
                    it[DiscordGatewayEvents.payload] = event.`package`.toString()
                }
            }
        }
    }
}