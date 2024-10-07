package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class NetworkLoriTuberVideo(
    @Serializable(UUIDSerializer::class)
    var id: UUID,
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