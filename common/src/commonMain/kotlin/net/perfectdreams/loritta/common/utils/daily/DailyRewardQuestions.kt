package net.perfectdreams.loritta.common.utils.daily

import net.perfectdreams.loritta.i18n.I18nKeysData

object DailyRewardQuestions {
    val all = listOf(
        DailyRewardQuestionWithAnswer(
            DailyRewardQuestion(
                "lori_cute",
                I18nKeysData.Daily.Questions.LoriCute.Title
            ),
            true
        ),
        DailyRewardQuestionWithAnswer(
            DailyRewardQuestion(
                "power_is_owner",
                I18nKeysData.Daily.Questions.PowerIsOwner.Title
            ),
            true
        ),
        DailyRewardQuestionWithAnswer(
            DailyRewardQuestion(
                "loritta_was_made_by_a_brazilian",
                I18nKeysData.Daily.Questions.LorittaWasMadeByABrazilian.Title
            ),
            true
        )
    )
}