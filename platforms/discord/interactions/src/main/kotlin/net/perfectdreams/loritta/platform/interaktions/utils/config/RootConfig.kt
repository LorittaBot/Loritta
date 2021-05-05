package net.perfectdreams.loritta.platform.interaktions.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.discord.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val interactions: DiscordInteractionsConfig,
    val services: ServicesConfig
)