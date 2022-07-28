package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PuddingConfig(
    val database: String,
    val address: String,
    val username: String,
    val password: String,
    val tablesAllowedToBeUpdated: List<String>? = null
)