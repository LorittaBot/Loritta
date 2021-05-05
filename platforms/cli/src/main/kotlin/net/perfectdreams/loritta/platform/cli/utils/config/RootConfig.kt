package net.perfectdreams.loritta.platform.cli.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val services: ServicesConfig
)