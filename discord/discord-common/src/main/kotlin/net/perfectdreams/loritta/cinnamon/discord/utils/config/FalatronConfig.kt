package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class FalatronConfig(
    val url: String,
    val key: String
)