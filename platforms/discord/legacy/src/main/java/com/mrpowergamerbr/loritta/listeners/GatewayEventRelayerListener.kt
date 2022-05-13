package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GatewayEventRelayerListener(val m: Loritta) : ListenerAdapter() {
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

    private val semaphore = Semaphore(4)

    override fun onRawGateway(event: RawGatewayEvent) {
        GlobalScope.launch(m.coroutineDispatcher) {
            // Just to avoid a LOT of threads being created at the same time, causing a "java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached"
            semaphore.withPermit {
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
}