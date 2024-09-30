package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class LoriTuberChannelData(
    var characterId: Long,
    var pendingVideoCounter: Long,
    var name: String,
    val pendingVideos: MutableList<LoriTuberPendingVideoData>,
    val channelRelationships: MutableMap<Long, LoriTuberSuperViewerChannelRelationshipData>,
    val channelRelationshipsV2: MutableMap<LoriTuberVideoContentCategory, LoriTuberSuperViewerChannelRelationshipData>
)