package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val pudding: PuddingConfig,
    val gabrielaImageServer: GabrielaImageServerConfig,
    val randomRoleplayPictures: RandomRoleplayPicturesConfig,
    val dreamStorageService: DreamStorageServiceConfig
)