package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RedisConfig(
    val keyPrefix: String,
    val address: String
)