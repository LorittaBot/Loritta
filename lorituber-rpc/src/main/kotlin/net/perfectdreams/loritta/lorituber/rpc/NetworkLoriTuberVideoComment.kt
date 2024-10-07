package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoCommentType

@Serializable
data class NetworkLoriTuberVideoComment(
    val postedAtTicksAfterVideoPost: Long,
    val viewerHandle: String,
    val commentType: LoriTuberVideoCommentType
)