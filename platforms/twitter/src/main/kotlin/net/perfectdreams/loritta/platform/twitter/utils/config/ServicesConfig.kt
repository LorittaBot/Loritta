package net.perfectdreams.loritta.cinnamon.platform.twitter.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val gabrielaImageServer: GabrielaImageServerConfig
)