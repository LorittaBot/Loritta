package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig

@Serializable
data class CinnamonConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val interactions: DiscordInteractionsConfig,
    val binaries: BinariesConfig,
    val falatron: FalatronConfig,
    val services: ServicesConfig,
    val prometheusPush: PrometheusPushConfig
)