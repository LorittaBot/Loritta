package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class GuildCustomCommandsConfig(
    val commands: List<GuildCustomCommand>
)