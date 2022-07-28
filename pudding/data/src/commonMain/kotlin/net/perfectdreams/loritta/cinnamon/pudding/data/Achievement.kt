package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.achievements.AchievementType

@Serializable
data class Achievement(
    val user: UserId,
    val type: net.perfectdreams.loritta.cinnamon.achievements.AchievementType,
    val achievedAt: Instant
)