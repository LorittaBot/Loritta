package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class CreatePendingVideoRequest(
    val channelId: Long,
    val contentCategory: LoriTuberVideoContentCategory,
    val contentVibes: LoriTuberVibes
) : LoriTuberRequest()

@Serializable
sealed class CreatePendingVideoResponse : LoriTuberResponse() {
    @Serializable
    data object Success : CreatePendingVideoResponse()

    @Serializable
    data object CharacterIsAlreadyDoingAnotherVideo : CreatePendingVideoResponse()

    @Serializable
    data object UnknownChannel : CreatePendingVideoResponse()
}