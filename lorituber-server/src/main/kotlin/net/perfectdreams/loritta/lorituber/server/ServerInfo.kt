package net.perfectdreams.loritta.lorituber.server

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val currentTick: Long,
    val lastUpdate: Long
)