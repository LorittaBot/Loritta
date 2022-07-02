package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class ReplicasConfig(
    val enabled: Boolean,
    val replicas: Int
)