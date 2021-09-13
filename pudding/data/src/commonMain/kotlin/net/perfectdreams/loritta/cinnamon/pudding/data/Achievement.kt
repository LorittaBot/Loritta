package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType

@Serializable
data class Achievement(
    val user: UserId,
    val type: AchievementType,
    val achievedAt: Instant
)