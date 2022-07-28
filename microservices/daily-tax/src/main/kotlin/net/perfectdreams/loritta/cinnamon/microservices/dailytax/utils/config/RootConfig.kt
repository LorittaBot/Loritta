package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val pudding: PuddingConfig
)