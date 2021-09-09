package net.perfectdreams.loritta.cinnamon.platform.twitter.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val twitter: TwitterConfig,
    val services: ServicesConfig
)