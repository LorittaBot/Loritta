package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.utils.config.CinnamonConfig

@Serializable
data class RootConfig(
    val cinnamon: CinnamonConfig,
    val httpInteractions: InteractionsEndpointConfig,
    val discordShards: DiscordShardsConfig,
    val replicas: ReplicasConfig,
    val eventProcessors: Int,
    val totalEventsPerBatch: Long
)