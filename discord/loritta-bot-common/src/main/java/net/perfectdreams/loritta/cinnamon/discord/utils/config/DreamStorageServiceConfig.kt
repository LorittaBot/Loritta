package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class DreamStorageServiceConfig(
    val url: String,
    val token: String
)