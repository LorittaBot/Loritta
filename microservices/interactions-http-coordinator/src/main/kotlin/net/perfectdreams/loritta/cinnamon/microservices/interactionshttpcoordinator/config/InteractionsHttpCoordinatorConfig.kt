package net.perfectdreams.loritta.cinnamon.microservices.interactionshttpcoordinator.config

import kotlinx.serialization.Serializable

@Serializable
data class InteractionsHttpCoordinatorConfig(
    val totalShards: Int,
    val instances: List<InteractionsHttpInstanceConfig>
)