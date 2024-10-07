package net.perfectdreams.loritta.serializable.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberTask {
    @Serializable
    class Sleeping : LoriTuberTask()

    @Serializable
    class Eating(val itemId: String, val startedEatingAtTick: Long) : LoriTuberTask()

    @Serializable
    class PreparingFood(
        /**
         * If null, then we are making slop
         */
        val recipeId: String?,
        val items: List<String>,
        val startedPreparingAtTick: Long
    ) : LoriTuberTask()

    @Serializable
    class WorkingOnVideo(val channelId: Long, val pendingVideoId: Long) : LoriTuberTask()
}