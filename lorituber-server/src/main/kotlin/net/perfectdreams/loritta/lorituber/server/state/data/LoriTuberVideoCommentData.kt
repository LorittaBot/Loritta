package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoCommentType

@Serializable
data class LoriTuberVideoCommentData(
    val postedAtTicksAfterVideoPost: Long,
    val viewerHandleId: Int,
    val commentType: LoriTuberVideoCommentType
)