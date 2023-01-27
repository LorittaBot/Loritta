package net.perfectdreams.loritta.common.utils.daily

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

@Serializable
data class DailyRewardQuestion(
    val id: String,
    val question: StringI18nData,
    val choices: List<DailyRewardChoice>,
    val incorrectExplanation: StringI18nData
) {
    @Serializable
    data class DailyRewardChoice(
        val text: StringI18nData,
        val correctAnswer: Boolean
    )
}