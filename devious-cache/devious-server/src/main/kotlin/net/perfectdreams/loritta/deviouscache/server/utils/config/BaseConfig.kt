package net.perfectdreams.loritta.deviouscache.server.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class BaseConfig(
    val persistenceDelay: Long,
    val certificateDomains: List<String>
)