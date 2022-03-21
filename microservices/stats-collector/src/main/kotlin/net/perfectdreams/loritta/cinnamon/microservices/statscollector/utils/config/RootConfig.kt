package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RootConfig(
    val lorittaLegacyClusterUrls: List<String>,
    val topgg: TopggConfig,
    val discordBots: DiscordBotsConfig,
    val pudding: PuddingConfig
)