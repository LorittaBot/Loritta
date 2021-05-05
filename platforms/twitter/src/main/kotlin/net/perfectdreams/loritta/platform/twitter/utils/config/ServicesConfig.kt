package net.perfectdreams.loritta.platform.twitter.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val gabrielaImageServer: GabrielaImageServerConfig
)