package net.perfectdreams.loritta.cinnamon.microservices.brokertickersupdater.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val database: String,
    val address: String,
    val username: String,
    val password: String
)