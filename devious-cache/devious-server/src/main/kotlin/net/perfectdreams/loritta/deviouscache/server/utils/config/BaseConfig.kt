package net.perfectdreams.loritta.deviouscache.server.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class BaseConfig(
    val host: String,
    val persistenceDelay: Long
)