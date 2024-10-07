package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class LoriTuberChannelData(
    @Serializable(UUIDSerializer::class)
    var characterId: UUID,
    var pendingVideoCounter: Long,
    var name: String,
    val pendingVideos: MutableList<LoriTuberPendingVideoData>,
    val categoryLevels: MutableMap<LoriTuberVideoContentCategory, Int>,
    val channelRelationshipsV2: MutableMap<LoriTuberVideoContentCategory, LoriTuberSuperViewerChannelRelationshipData>
)