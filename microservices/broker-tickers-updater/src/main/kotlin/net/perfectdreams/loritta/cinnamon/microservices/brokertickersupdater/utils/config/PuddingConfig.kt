package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val database: String? = null,
    val address: String? = null,
    val username: String? = null,
    val password: String? = null
)