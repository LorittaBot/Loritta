package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfigRoot(
    val id: ULong,
    val localeId: String,
    val starboardConfigId: Long?
)