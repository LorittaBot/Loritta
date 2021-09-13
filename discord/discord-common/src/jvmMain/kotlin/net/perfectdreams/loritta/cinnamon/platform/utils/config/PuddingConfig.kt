package net.perfectdreams.loritta.cinnamon.platform.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.DatabaseType

@Serializable
data class PuddingConfig(
    val type: DatabaseType,
    val database: String? = null,
    val address: String? = null,
    val username: String? = null,
    val password: String? = null,
    val tablesAllowedToBeUpdated: List<String>? = null
)