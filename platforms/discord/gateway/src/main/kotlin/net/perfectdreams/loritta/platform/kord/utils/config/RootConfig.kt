package net.perfectdreams.loritta.platform.kord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.discord.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val services: ServicesConfig
)