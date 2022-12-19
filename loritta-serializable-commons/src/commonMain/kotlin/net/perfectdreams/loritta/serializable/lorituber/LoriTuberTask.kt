package net.perfectdreams.loritta.serializable.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberTask {
    @Serializable
    class Sleeping : LoriTuberTask()

    @Serializable
    class WorkingOnVideo(val channelId: Long, val pendingVideoId: Long) : LoriTuberTask()
}