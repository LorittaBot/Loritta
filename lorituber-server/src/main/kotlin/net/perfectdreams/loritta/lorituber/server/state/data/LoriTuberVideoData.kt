package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class LoriTuberVideoData(
    @Serializable(UUIDSerializer::class)
    var channelId: UUID,
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