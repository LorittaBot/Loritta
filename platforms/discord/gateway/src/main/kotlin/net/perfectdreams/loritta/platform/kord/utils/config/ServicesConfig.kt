package net.perfectdreams.loritta.platform.kord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val lorittaData: LorittaDataConfig,
    val gabrielaImageServer: GabrielaImageServerConfig
)