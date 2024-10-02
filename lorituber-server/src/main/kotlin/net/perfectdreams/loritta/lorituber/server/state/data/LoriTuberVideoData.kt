package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class LoriTuberVideoData(
    var channelId: Long,
    var title: String,
    var public: Boolean,
    var postedAtTicks: Long,
    var contentCategory: LoriTuberVideoContentCategory,
    var contentScore: Int,
    var recordingScore: Int,
    var editingScore: Int,
    var thumbnailScore: Int,
    var vibes: LoriTuberVibes,
    var vibesAtTheTime: LoriTuberVibes,
    var views: Int,
    var likes: Int,
    var dislikes: Int,
    var videoEvents: Map<Long, List<LoriTuberVideoEvent>>,
    var comments: List<LoriTuberVideoCommentData>
)