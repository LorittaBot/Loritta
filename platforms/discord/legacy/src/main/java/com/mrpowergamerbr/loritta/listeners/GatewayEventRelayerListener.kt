package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
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

    val channel: Channel
    init {
        val factory = ConnectionFactory()
        factory.host = m.config.rabbitMQ.host
        factory.virtualHost = m.config.rabbitMQ.virtualHost
        factory.username = m.config.rabbitMQ.username
        factory.password = m.config.rabbitMQ.password
        factory.isAutomaticRecoveryEnabled = true

        val connection = factory.newConnection()
        channel = connection.createChannel()
    }

    override fun onRawGateway(event: RawGatewayEvent) {
        if (event.type !in RELAYED_EVENTS)
            return

        GlobalScope.launch(m.coroutineDispatcher) {
            val routingKey = "event.${event.type.replace("_", "-").lowercase()}"

            // Publish the event to the events exchange
            channel.basicPublish(
                "discord-gateway-events",
                routingKey,
                null,
                event.`package`.toString().toByteArray(Charsets.UTF_8)
            )
        }
    }
}