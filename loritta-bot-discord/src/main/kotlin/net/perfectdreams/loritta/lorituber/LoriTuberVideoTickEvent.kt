package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberVideoTickEvent {
    @Serializable
    data class UpdateEngagementEvent(
        val newViews: Int,
        val newLikes: Int,
        val newDislikes: Int
    ) : LoriTuberVideoTickEvent()
}