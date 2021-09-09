package net.perfectdreams.loritta.cinnamon.platform.kord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val services: ServicesConfig
)