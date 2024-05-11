package net.perfectdreams.loritta.morenitta.website.utils

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FavoritedGuild(
    val guildId: Long,
    val favoritedAt: Instant
)