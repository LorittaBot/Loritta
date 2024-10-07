package net.perfectdreams.loritta.lorituber.server.state

import kotlinx.serialization.Serializable

@Serializable
data class WorldInfo(
    var currentTick: Long,
    var lastUpdate: Long
)