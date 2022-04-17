package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.ProcessDiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager
) {
    companion object {
        const val RABBITMQ_EXCHANGE_NAME = "discord-gateway-events"
    }

    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)

    fun start() {
        val factory = ConnectionFactory()
        val (ip, port) = config.rabbitMQ.address.split(":")
        factory.host = ip
        factory.port = port.toInt()
        factory.virtualHost = config.rabbitMQ.virtualHost
        factory.username = config.rabbitMQ.username
        factory.password = config.rabbitMQ.password
        factory.isAutomaticRecoveryEnabled = true

        val connection = factory.newConnection()
        val channel = connection.createChannel()

        // Setup the exchange
        channel.exchangeDeclare(RABBITMQ_EXCHANGE_NAME, BuiltinExchangeType.TOPIC)

        starboardModule.setupConsumer(channel)
        addFirstToNewChannelsModule.setupConsumer(channel)
    }
}