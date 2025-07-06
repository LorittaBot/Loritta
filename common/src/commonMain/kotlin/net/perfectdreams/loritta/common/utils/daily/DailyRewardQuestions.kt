package net.perfectdreams.loritta.common.utils.daily

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData

object DailyRewardQuestions {
    val all = questions {
        question("lori_cute", I18nKeysData.Daily.Questions.LoriCute.Title, I18nKeysData.Daily.Questions.LoriCute.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.LoriCute.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.LoriCute.No, correctAnswer = false)
        }

        question("power_is_owner", I18nKeysData.Daily.Questions.PowerIsOwner.Title, I18nKeysData.Daily.Questions.PowerIsOwner.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.PowerIsOwner.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.PowerIsOwner.No, correctAnswer = false)
        }

        question("loritta_was_made_by_a_brazilian", I18nKeysData.Daily.Questions.LorittaWasMadeByABrazilian.Title, I18nKeysData.Daily.Questions.LorittaWasMadeByABrazilian.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.LorittaWasMadeByABrazilian.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.LorittaWasMadeByABrazilian.No, correctAnswer = false)
        }

        question("nsfw_content", I18nKeysData.Daily.Questions.NsfwContent.Title, I18nKeysData.Daily.Questions.NsfwContent.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.NsfwContent.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.NsfwContent.No, correctAnswer = false)
        }

        question("sparklypower", I18nKeysData.Daily.Questions.SparklyPower.Title, I18nKeysData.Daily.Questions.SparklyPower.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.SparklyPower.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.SparklyPower.No, correctAnswer = false)
        }

        question("multiple_accounts_daily", I18nKeysData.Daily.Questions.MultipleAccountsDaily.Title, I18nKeysData.Daily.Questions.MultipleAccountsDaily.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.MultipleAccountsDaily.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.MultipleAccountsDaily.No, correctAnswer = false)
        }

        question("multiple_accounts_daily", I18nKeysData.Daily.Questions.MultipleAccountsDaily.Title, I18nKeysData.Daily.Questions.MultipleAccountsDaily.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.MultipleAccountsDaily.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.MultipleAccountsDaily.No, correctAnswer = false)
        }

        question("external_sonhos_trade", I18nKeysData.Daily.Questions.ExternalSonhosTrade.Title, I18nKeysData.Daily.Questions.ExternalSonhosTrade.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.ExternalSonhosTrade.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.ExternalSonhosTrade.No, correctAnswer = false)
        }

        question("external_account_sell", I18nKeysData.Daily.Questions.ExternalAccountSell.Title, I18nKeysData.Daily.Questions.ExternalAccountSell.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.ExternalAccountSell.IfTheAccountIsNotMineSure, correctAnswer = false)
            choice(I18nKeysData.Daily.Questions.ExternalAccountSell.YesEvenYouTubeAccounts, correctAnswer = true)
        }

        question("report_to_server_staff_if_you_are_offered_sonhos", I18nKeysData.Daily.Questions.ReportToServerStaffIfYouAreOfferedSonhos.Title, I18nKeysData.Daily.Questions.ReportToServerStaffIfYouAreOfferedSonhos.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.ReportToServerStaffIfYouAreOfferedSonhos.OfCourse, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.ReportToServerStaffIfYouAreOfferedSonhos.IWont, correctAnswer = false)
        }

        question("lori_staff_protect_buying_sonhos_outside_of_the_website", I18nKeysData.Daily.Questions.LoriStaffProtectBuyingSonhosOutsideOfTheWebsite.Title, I18nKeysData.Daily.Questions.LoriStaffProtectBuyingSonhosOutsideOfTheWebsite.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.LoriStaffProtectBuyingSonhosOutsideOfTheWebsite.WeDoProtect, correctAnswer = false)
            choice(I18nKeysData.Daily.Questions.LoriStaffProtectBuyingSonhosOutsideOfTheWebsite.WeDontProtect, correctAnswer = true)
        }

        question("sell_nitro", I18nKeysData.Daily.Questions.SellDiscordNitro.Title, I18nKeysData.Daily.Questions.SellDiscordNitro.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.SellDiscordNitro.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.SellDiscordNitro.No, correctAnswer = false)
        }

        question("command_bugs", I18nKeysData.Daily.Questions.CommandBugs.Title, I18nKeysData.Daily.Questions.CommandBugs.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.CommandBugs.Yes, correctAnswer = false)
            choice(I18nKeysData.Daily.Questions.CommandBugs.No, correctAnswer = true)
        }

        question("command_bugs_win", I18nKeysData.Daily.Questions.CommandBugsWin.Title, I18nKeysData.Daily.Questions.CommandBugsWin.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.CommandBugsWin.Yes, correctAnswer = false)
            choice(I18nKeysData.Daily.Questions.CommandBugsWin.No, correctAnswer = true)
        }

        question("ban_evasion_report", I18nKeysData.Daily.Questions.BanEvasionReport.Title, I18nKeysData.Daily.Questions.BanEvasionReport.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.BanEvasionReport.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.BanEvasionReport.No, correctAnswer = false)
        }

        question("invites_for_sonhos", I18nKeysData.Daily.Questions.InvitesForSonhos.Title, I18nKeysData.Daily.Questions.InvitesForSonhos.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.InvitesForSonhos.YesAndTheStaffToo, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.InvitesForSonhos.YouArentGoingToGetBanned, correctAnswer = false)
        }

        question("will_i_get_banned_if_i_create_a_new_account", I18nKeysData.Daily.Questions.WillIGetBannedIfICreateANewAccount.Title, I18nKeysData.Daily.Questions.WillIGetBannedIfICreateANewAccount.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.WillIGetBannedIfICreateANewAccount.YesICanDoThis, correctAnswer = false)
            choice(I18nKeysData.Daily.Questions.WillIGetBannedIfICreateANewAccount.NoBecauseIWillGetBannedAgain, correctAnswer = true)
        }

        question("asking_my_friend_to_ask_for_my_unban", I18nKeysData.Daily.Questions.AskingMyFriendToAskForMyUnban.Title, I18nKeysData.Daily.Questions.AskingMyFriendToAskForMyUnban.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.AskingMyFriendToAskForMyUnban.YesTheyCan, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.AskingMyFriendToAskForMyUnban.NoTheyCant, correctAnswer = false)
        }

        question("chargeback_ban", I18nKeysData.Daily.Questions.ChargebackBan.Title, I18nKeysData.Daily.Questions.ChargebackBan.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.ChargebackBan.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.ChargebackBan.No, correctAnswer = false)
        }

        question("sonhos_threat", I18nKeysData.Daily.Questions.SonhosThreat.Title, I18nKeysData.Daily.Questions.SonhosThreat.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.SonhosThreat.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.SonhosThreat.No, correctAnswer = false)
        }

        question("found_bugs", I18nKeysData.Daily.Questions.FoundBugs.Title, I18nKeysData.Daily.Questions.FoundBugs.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.FoundBugs.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.FoundBugs.No, correctAnswer = false)
        }

        question("stupid_moneylender", I18nKeysData.Daily.Questions.StupidMoneylender.Title, I18nKeysData.Daily.Questions.StupidMoneylender.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.StupidMoneylender.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.StupidMoneylender.No, correctAnswer = false)
        }

        question("caution_when_receiving_too_many_sonhos", I18nKeysData.Daily.Questions.CautionWhenReceivingTooManySonhos.Title, I18nKeysData.Daily.Questions.CautionWhenReceivingTooManySonhos.IncorrectExplanation) {
            choice(I18nKeysData.Daily.Questions.CautionWhenReceivingTooManySonhos.Yes, correctAnswer = true)
            choice(I18nKeysData.Daily.Questions.CautionWhenReceivingTooManySonhos.No, correctAnswer = false)
        }
    }

    private fun questions(builder: QuestionsBuilder.() -> (Unit)): List<DailyRewardQuestion> {
        return QuestionsBuilder().apply(builder).questions
    }

    private class QuestionsBuilder {
        val questions = mutableListOf<DailyRewardQuestion>()

        fun question(id: String, title: StringI18nData, incorrectText: StringI18nData, builder: QuestionBuilder.() -> (Unit) = {}) {
            questions.add(
                QuestionBuilder(id, title, incorrectText).apply(builder)
                    .build()
            )
        }

        class QuestionBuilder(val id: String, val title: StringI18nData, val incorrectExplanation: StringI18nData) {
            val choices = mutableListOf<DailyRewardQuestion.DailyRewardChoice>()

            fun choice(title: StringI18nData, correctAnswer: Boolean) {
                choices.add(
                    DailyRewardQuestion.DailyRewardChoice(
                        title,
                        correctAnswer
                    )
                )
            }

            fun build() = DailyRewardQuestion(
                id,
                title,
                choices,
                incorrectExplanation
            )
        }
    }
}