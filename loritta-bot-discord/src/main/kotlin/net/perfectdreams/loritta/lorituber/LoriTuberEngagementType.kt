package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable

@Serializable
sealed class LoriTuberEngagementType {
    // Changing the engagementStartTick causes the easing simulation to change
    abstract val engagementStartTick: Long
    abstract val engagementEndTick: Long
    abstract val startViews: Int
    abstract val startLikes: Int
    abstract val startDislikes: Int
    abstract val targetViews: Int
    abstract val targetLikes: Int
    abstract val targetDislikes: Int

    abstract fun simulateEngagement(currentTick: Long, engagementCycleProgress: Double): SimulatedEngagement

    @Serializable
    class EaseOutQuint(
        override val engagementStartTick: Long,
        override val engagementEndTick: Long,
        override val startViews: Int,
        override val startLikes: Int,
        override val startDislikes: Int,
        override val targetViews: Int,
        override val targetLikes: Int,
        override val targetDislikes: Int
    ): LoriTuberEngagementType() {
        override fun simulateEngagement(currentTick: Long, engagementCycleProgress: Double): SimulatedEngagement {
            val engagementCycleViews = targetViews - startViews
            val engagementCycleLikes = targetLikes - startLikes
            val engagementCycleDislikes = targetDislikes - startDislikes

            val easedEngagement = easeOutQuint(engagementCycleProgress)

            return SimulatedEngagement(
                (engagementCycleViews * easedEngagement).toInt(),
                (engagementCycleLikes * easedEngagement).toInt(),
                (engagementCycleDislikes * easedEngagement).toInt()
            )
        }

        private fun easeOutQuint(x: Double): Double {
            return 1 - Math.pow(1 - x, 5.0)
        }
    }

    @Serializable
    class Linear(
        override val engagementStartTick: Long,
        override val engagementEndTick: Long,
        override val startViews: Int,
        override val startLikes: Int,
        override val startDislikes: Int,
        override val targetViews: Int,
        override val targetLikes: Int,
        override val targetDislikes: Int
    ): LoriTuberEngagementType() {
        override fun simulateEngagement(currentTick: Long, engagementCycleProgress: Double): SimulatedEngagement {
            val engagementCycleViews = targetViews - startViews
            val engagementCycleLikes = targetLikes - startLikes
            val engagementCycleDislikes = targetDislikes - startDislikes

            return SimulatedEngagement(
                (engagementCycleViews * engagementCycleProgress).toInt(),
                (engagementCycleLikes * engagementCycleProgress).toInt(),
                (engagementCycleDislikes * engagementCycleProgress).toInt()
            )
        }
    }

    data class SimulatedEngagement(
        val views: Int,
        val likes: Int,
        val dislikes: Int
    )
}