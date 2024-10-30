package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable

@Serializable
data class GuildReactionEventsConfig(
    val enabled: Boolean
)