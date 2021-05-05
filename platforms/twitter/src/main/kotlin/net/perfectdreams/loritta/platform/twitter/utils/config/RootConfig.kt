package net.perfectdreams.loritta.platform.twitter.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val twitter: TwitterConfig,
    val services: ServicesConfig
)