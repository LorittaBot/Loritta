package net.perfectdreams.loritta.cinnamon.platform.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val pudding: PuddingConfig,
    val gabrielaImageServer: GabrielaImageServerConfig,
    val randomRoleplayPictures: RandomRoleplayPicturesConfig,
)