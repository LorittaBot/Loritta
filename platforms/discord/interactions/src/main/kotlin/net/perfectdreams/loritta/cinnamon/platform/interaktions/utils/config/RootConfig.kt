package net.perfectdreams.loritta.cinnamon.platform.interaktions.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val interactions: DiscordInteractionsConfig,
    val services: ServicesConfig
)