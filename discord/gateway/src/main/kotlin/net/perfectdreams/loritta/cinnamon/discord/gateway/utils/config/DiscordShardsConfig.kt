package net.perfectdreams.loritta.cinnamon.discord.gateway.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordShardsConfig(
    val minShard: Int,
    val maxShard: Int,
    val totalShards: Int
)