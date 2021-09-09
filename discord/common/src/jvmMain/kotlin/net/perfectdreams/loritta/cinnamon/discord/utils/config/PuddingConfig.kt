package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val url: String,
    val authorization: String
)