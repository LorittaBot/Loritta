package net.perfectdreams.loritta.cinnamon.platform.cli.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val lorittaData: LorittaDataConfig,
    val gabrielaImageServer: GabrielaImageServerConfig
)