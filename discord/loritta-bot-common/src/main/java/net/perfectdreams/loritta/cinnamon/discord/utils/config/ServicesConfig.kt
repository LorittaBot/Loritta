package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig

@Serializable
class ServicesConfig(
    val pudding: PuddingConfig,
    val redis: RedisConfig,
    val googleVision: GoogleVisionConfig,
    val gabrielaImageServer: GabrielaImageServerConfig,
    val randomRoleplayPictures: RandomRoleplayPicturesConfig,
    val dreamStorageService: DreamStorageServiceConfig
)