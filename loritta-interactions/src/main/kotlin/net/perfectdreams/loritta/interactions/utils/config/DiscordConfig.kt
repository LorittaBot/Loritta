package net.perfectdreams.loritta.interactions.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordConfig(
    val applicationId: Long,
    val publicKey: String,
    val token: String
)