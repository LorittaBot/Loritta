package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RabbitMQConfig(
    val address: String,
    val virtualHost: String,
    val username: String,
    val password: String
)