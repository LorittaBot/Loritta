package net.perfectdreams.loritta.cinnamon.showtime.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDiscordConfig(
    val applicationId: Long
)