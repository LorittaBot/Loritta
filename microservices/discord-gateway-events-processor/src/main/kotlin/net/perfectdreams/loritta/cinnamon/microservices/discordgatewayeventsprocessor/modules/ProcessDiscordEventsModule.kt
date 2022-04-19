package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import dev.kord.gateway.Event
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.KordDiscordEventUtils

abstract class ProcessDiscordEventsModule(private val rabbitMQQueue: String) {
    abstract fun processEvent(event: Event)

    /**
     * Setups the required queue binds for this module on the [channel]
     */
    abstract fun setupQueueBinds(channel: Channel)

    /**
     * Setups a RabbitMQ consumer for this module on the [channel]
     */
    fun setupConsumer(channel: Channel) {
        channel.queueDeclare(rabbitMQQueue, true, false, false, null)
        setupQueueBinds(channel)

        channel.basicConsume(
            rabbitMQQueue,
            true,
            DeliverCallback { consumerTag, message ->
                val discordEvent = KordDiscordEventUtils.parseEventFromJsonString(message.body.toString(Charsets.UTF_8))
                if (discordEvent != null)
                    processEvent(discordEvent)
            },
            CancelCallback {

            }
        )
    }

    /**
     * Helper method to bind a queue using [Channel.queueBind], providing [rabbitMQQueue] as the queue and [DiscordGatewayEventsProcessor.RABBITMQ_EXCHANGE_NAME] as the exchange name
     *
     * @see Channel.queueBind
     */
    fun Channel.queueBindToModuleQueue(routingKey: String): AMQP.Queue.BindOk = this.queueBind(rabbitMQQueue, DiscordGatewayEventsProcessor.RABBITMQ_EXCHANGE_NAME, routingKey)
}