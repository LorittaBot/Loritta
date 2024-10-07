package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class StartWorkingOnPendingVideoRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    @Serializable(UUIDSerializer::class)
    val channelId: UUID,
    val pendingVideoId: Long,
    val stage: LoriTuberVideoStage
) : LoriTuberRequest()

@Serializable
sealed class StartWorkingOnPendingVideoResponse : LoriTuberResponse() {
    @Serializable
    data object Success : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object MoodTooLow : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object UnknownChannel : StartWorkingOnPendingVideoResponse()

    @Serializable
    data object UnknownPendingVideo : StartWorkingOnPendingVideoResponse()
}