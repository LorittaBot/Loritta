package net.perfectdreams.loritta.website.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class LorittaDiscordConfig(
    val applicationId: Long,
    val clientSecret: String
)