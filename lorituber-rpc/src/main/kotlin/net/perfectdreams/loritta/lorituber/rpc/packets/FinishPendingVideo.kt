package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class FinishPendingVideoRequest(
    @Serializable(UUIDSerializer::class)
    val channelId: UUID,
    val pendingVideoId: Long,
    val videoTitle: String
) : LoriTuberRequest()

@Serializable
sealed class FinishPendingVideoResponse : LoriTuberResponse() {
    @Serializable
    data object Success : FinishPendingVideoResponse()

    @Serializable
    data object UnknownChannel : FinishPendingVideoResponse()

    @Serializable
    data object UnknownPendingVideo : FinishPendingVideoResponse()
}