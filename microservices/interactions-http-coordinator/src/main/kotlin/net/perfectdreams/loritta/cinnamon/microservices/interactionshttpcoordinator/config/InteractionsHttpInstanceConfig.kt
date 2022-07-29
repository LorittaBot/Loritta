package net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator.config

import kotlinx.serialization.Serializable

@Serializable
data class InteractionsHttpInstanceConfig(
    val minShard: Int,
    val maxShard: Int,
    val url: String
)