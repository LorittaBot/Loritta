package net.perfectdreams.loritta.cinnamon.platform.utils.config

import kotlinx.serialization.Serializable

@Serializable
class LorittaDiscordConfig(
    val token: String,
    val applicationId: Long
)