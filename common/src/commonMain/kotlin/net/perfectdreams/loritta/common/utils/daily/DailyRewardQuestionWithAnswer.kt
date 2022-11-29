package net.perfectdreams.loritta.common.utils.daily

import kotlinx.serialization.Serializable

@Serializable
data class DailyRewardQuestionWithAnswer(
    val question: DailyRewardQuestion,
    val correctAnswer: Boolean
)