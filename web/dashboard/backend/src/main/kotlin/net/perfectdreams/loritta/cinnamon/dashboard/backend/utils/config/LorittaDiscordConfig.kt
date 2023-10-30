package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDiscordConfig(
    val applicationId: Long,
    val clientSecret: String
)