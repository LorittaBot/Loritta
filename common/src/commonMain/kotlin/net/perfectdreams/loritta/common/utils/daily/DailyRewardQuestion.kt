package net.perfectdreams.loritta.common.utils.daily

import kotlinx.serialization.Serializable

@Serializable
data class DailyRewardQuestion(
    val id: String,
    val question: String
)