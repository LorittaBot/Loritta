package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val currentTick: Long,
    val lastUpdate: Long,
    val averageTickDuration: Double?
)