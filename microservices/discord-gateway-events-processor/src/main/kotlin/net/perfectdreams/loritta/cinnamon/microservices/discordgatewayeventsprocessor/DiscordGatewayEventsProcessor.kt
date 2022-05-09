package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.ExceptionHandler
import com.rabbitmq.client.Recoverable
import com.rabbitmq.client.RecoveryListener
import com.rabbitmq.client.TopologyRecoveryException
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager
) {
    companion object {
        const val RABBITMQ_EXCHANGE_NAME = "discord-gateway-events"
        private val logger = KotlinLogging.logger {}
    }

    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    val tasks = DiscordGatewayEventsProcessorTasks(this)

    fun start() {
        val factory = ConnectionFactory()
        val (ip, port) = config.rabbitMQ.address.split(":")
        factory.host = ip
        factory.port = port.toInt()
        factory.virtualHost = config.rabbitMQ.virtualHost
        factory.username = config.rabbitMQ.username
        factory.password = config.rabbitMQ.password

        // Automatic recovery and topology recovery only works on NETWORK errors, not when RabbitMQ shut downs
        factory.isAutomaticRecoveryEnabled = true
        factory.isTopologyRecoveryEnabled = true

        factory.exceptionHandler = object: ExceptionHandler {
            override fun handleUnexpectedConnectionDriverException(conn: Connection?, exception: Throwable?) {
                logger.error(exception) { "Unexpected connection driver exception on $conn" }
            }

            override fun handleReturnListenerException(channel: Channel, exception: Throwable) {
                logger.error(exception) { "Channel return listener exception on $channel" }
            }

            override fun handleConfirmListenerException(channel: Channel, exception: Throwable) {
                logger.error(exception) { "Channel confirm listener exception on $channel" }
            }

            override fun handleBlockedListenerException(connection: Connection, exception: Throwable) {
                logger.error(exception) { "Connection blocked listener exception on $connection" }
            }

            override fun handleConsumerException(
                channel: Channel,
                exception: Throwable,
                consumer: Consumer,
                consumerTag: String,
                methodName: String
            ) {
                logger.error(exception) { "Consumer exception on $consumer on channel $channel, consumerTag \"$consumerTag\" and methodName \"$methodName\"" }
            }

            override fun handleConnectionRecoveryException(conn: Connection, exception: Throwable) {
                logger.error(exception) { "Connection recovery exception on $conn" }
            }

            override fun handleChannelRecoveryException(ch: Channel?, exception: Throwable?) {
                logger.error(exception) { "Channel recovery exception on $ch" }
            }

            override fun handleTopologyRecoveryException(
                conn: Connection,
                ch: Channel,
                exception: TopologyRecoveryException
            ) {
                logger.error(exception) { "Topology recovery exception on connection $conn and channel $ch" }
            }
        }

        val connection = factory.newConnection("discord-gateway-events-processor-connection")

        connection as Recoverable
        connection.addRecoveryListener(object: RecoveryListener {
            override fun handleRecovery(recoverable: Recoverable) {
                logger.info { "Connection $recoverable has been successfully recovered!" }
            }

            override fun handleRecoveryStarted(recoverable: Recoverable) {
                logger.info { "Connection $recoverable seems to have failed... Trying to recover it!" }
            }
        })

        val channel = connection.createChannel()

        // Setup the exchange
        channel.exchangeDeclare(RABBITMQ_EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true)

        starboardModule.setupConsumer(channel)
        addFirstToNewChannelsModule.setupConsumer(channel)

        tasks.start()
    }
}