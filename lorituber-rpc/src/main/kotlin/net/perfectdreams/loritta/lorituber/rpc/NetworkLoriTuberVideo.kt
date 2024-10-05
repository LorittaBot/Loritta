package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class NetworkLoriTuberVideo(
    var id: Long,
    var title: String,
    var postedAtTicks: Long,
    var contentCategory: LoriTuberVideoContentCategory,

    var vibes: LoriTuberVibes,
    var matchedVibes: Int,

    var recordingScore: Int,
    var editingScore: Int,
    var thumbnailScore: Int,

    var views: Int,
    var likes: Int,
    var dislikes: Int,

    val comments: List<NetworkLoriTuberVideoComment>
)