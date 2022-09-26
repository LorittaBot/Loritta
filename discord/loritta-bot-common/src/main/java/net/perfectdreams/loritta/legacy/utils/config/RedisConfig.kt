package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RedisConfig(
    val keyPrefix: String,
    val address: String,
    val password: String?
)