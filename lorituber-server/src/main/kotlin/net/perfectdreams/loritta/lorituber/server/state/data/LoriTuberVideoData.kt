package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class LoriTuberVideoData(
    var channelId: Long,
    var public: Boolean,
    var postedAtTicks: Long,
    var contentCategory: LoriTuberVideoContentCategory,
    var contentLength: LoriTuberContentLength,
    var recordingScore: Int,
    var editingScore: Int,
    var thumbnailScore: Int,
    var vibes: LoriTuberVibes,
    var views: Int,
    var likes: Int,
    var dislikes: Int,
    var videoEvents: Map<Long, List<LoriTuberVideoEvent>>
)