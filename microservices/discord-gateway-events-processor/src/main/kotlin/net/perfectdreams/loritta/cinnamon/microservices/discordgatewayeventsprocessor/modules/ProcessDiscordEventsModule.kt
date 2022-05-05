package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import dev.kord.gateway.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.KordDiscordEventUtils
import java.util.concurrent.ConcurrentLinkedQueue

abstract class ProcessDiscordEventsModule(private val rabbitMQQueue: String) {
    companion object {
        private val logger = KotlinLogging.logger {}

        const val MAX_ACTIVE_EVENTS_THRESHOLD = 16
    }

    val activeEvents = ConcurrentLinkedQueue<Job>()
    var launchedEvents = 0
    var discardedEvents = 0

    abstract suspend fun processEvent(event: Event)

    /**
     * Setups the required queue binds for this module on the [channel]
     */
    abstract fun setupQueueBinds(channel: Channel)

    /**
     * Setups a RabbitMQ consumer for this module on the [channel]
     */
    fun setupConsumer(channel: Channel) {
        val thisClass = this::class.simpleName

        channel.queueDeclare(rabbitMQQueue, true, false, false, null)
        setupQueueBinds(channel)

        channel.basicConsume(
            rabbitMQQueue,
            false,
            DeliverCallback { consumerTag, message ->
                val discordEvent = KordDiscordEventUtils.parseEventFromJsonString(message.body.toString(Charsets.UTF_8))
                if (discordEvent != null) {
                    if (16 >= activeEvents.size) {
                        launchedEvents++
                        launchEventJob("Event ${discordEvent::class.simpleName} for $thisClass") {
                            processEvent(discordEvent)

                            channel.basicAck(message.envelope.deliveryTag, false)
                        }
                    } else {
                        discardedEvents++
                        logger.warn { "There are more than $MAX_ACTIVE_EVENTS_THRESHOLD active events! We will nack the event and requeue them... ($activeEvents)" }
                        channel.basicNack(message.envelope.deliveryTag, false, true)
                    }
                }
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

    fun launchEventJob(coroutineName: String, block: suspend CoroutineScope.() -> Unit) {
        val start = System.currentTimeMillis()
        val job = GlobalScope.launch(
            CoroutineName(coroutineName),
            block = block
        )

        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        activeEvents.add(job)
        job.invokeOnCompletion {
            activeEvents.remove(job)

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Message Coroutine $job took too long to process! ${diff}ms" }
            }
        }
    }
}