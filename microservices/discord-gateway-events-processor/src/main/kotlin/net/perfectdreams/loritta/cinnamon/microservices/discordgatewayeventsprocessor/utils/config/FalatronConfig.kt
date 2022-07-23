package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class FalatronConfig(
    val url: String
)