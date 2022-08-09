package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.utils.config.CinnamonConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.PuddingConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

@Serializable
data class RootConfig(
    val cinnamon: CinnamonConfig,
    val httpInteractions: InteractionsEndpointConfig,
    val discordShards: DiscordShardsConfig,
    val replicas: ReplicasConfig,
    val queueDatabase: QueueDatabaseConfig,
    val totalEventsPerBatch: Int,
)