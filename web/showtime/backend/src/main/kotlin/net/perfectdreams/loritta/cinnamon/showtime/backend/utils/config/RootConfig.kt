package net.perfectdreams.loritta.cinnamon.showtime.backend.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.PuddingConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val etherealGambi: EtherealGambiConfig,
    val pudding: PuddingConfig
)