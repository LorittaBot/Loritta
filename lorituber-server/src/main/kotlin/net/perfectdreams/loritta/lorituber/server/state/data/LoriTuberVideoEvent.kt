package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberVideoEvent {
    @Serializable
    data class AddEngagement(
        val views: Int,
        val likes: Int,
        val dislikes: Int
    ) : LoriTuberVideoEvent()
}