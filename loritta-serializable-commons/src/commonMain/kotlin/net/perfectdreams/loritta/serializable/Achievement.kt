package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.achievements.AchievementType

@Serializable
data class Achievement(
    val user: UserId,
    val type: AchievementType,
    val achievedAt: Instant
)