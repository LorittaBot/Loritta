package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.EmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object EmojiFightBetSonhosTransactionTransformer : SonhosTransactionTransformer<EmojiFightBetSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: EmojiFightBetSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val wonTheBet = transaction.user == transaction.winner
        val winnerUserInfo = cachedUserInfos.getOrPut(transaction.winner) { loritta.getCachedUserInfo(transaction.winner) }
        // We don't store the loser because there may be multiple losers, so we only check that if the user isn't the "winner", then they are the loser
        val loserUserInfo = cachedUserInfo.id

        val userCountExcludingTheWinner = transaction.usersInMatch - 1

        if (transaction.tax != null && transaction.taxPercentage != null) {
            // Taxed earning
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.EmojiFightBet.WonTaxed(
                            quantity = transaction.entryPrice * userCountExcludingTheWinner,
                            quantityAfterTax = transaction.entryPriceAfterTax * userCountExcludingTheWinner,
                            userInEmojiFight = userCountExcludingTheWinner,
                            emojiFightEmoji = transaction.emoji
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.EmojiFightBet.LostTaxed(
                            quantity = transaction.entryPrice,
                            quantityAfterTax = transaction.entryPriceAfterTax,
                            winnerTag = "${winnerUserInfo?.name?.stripCodeBackticks()}#${winnerUserInfo?.discriminator}",
                            winnerId = transaction.winner.value,
                            emojiFightEmoji = transaction.emoji
                        )
                    )
                )
            }
        } else {
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.EmojiFightBet.Won(
                            quantityAfterTax = transaction.entryPriceAfterTax * userCountExcludingTheWinner,
                            userInEmojiFight = userCountExcludingTheWinner,
                            emojiFightEmoji = transaction.emoji
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.EmojiFightBet.Lost(
                            quantity = transaction.entryPrice,
                            winnerTag = "${winnerUserInfo?.name?.stripCodeBackticks()}#${winnerUserInfo?.discriminator}",
                            winnerId = transaction.winner.value,
                            emojiFightEmoji = transaction.emoji
                        )
                    )
                )
            }
        }
    }
}