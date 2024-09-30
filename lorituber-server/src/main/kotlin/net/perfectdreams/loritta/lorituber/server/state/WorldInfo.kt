package net.perfectdreams.loritta.lorituber.server.state

import kotlinx.serialization.Serializable

@Serializable
data class WorldInfo(
    var currentTick: Long,
    var lastUpdate: Long,
    var characterCounter: Long,
    var channelCounter: Long,
    var videoCounter: Long,
    var superViewerCounter: Long
)