package net.perfectdreams.loritta.platform.interaktions.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val lorittaData: LorittaDataConfig,
    val gabrielaImageServer: GabrielaImageServerConfig
)