package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.CoinFlipBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object CoinFlipBetSonhosTransactionTransformer : SonhosTransactionTransformer<CoinFlipBetSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: CoinFlipBetSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val wonTheBet = transaction.user == transaction.winner
        val winnerUserInfo = cachedUserInfos.getOrPut(transaction.winner) { loritta.getCachedUserInfo(transaction.winner) }
        val loserUserInfo = cachedUserInfos.getOrPut(transaction.loser) { loritta.getCachedUserInfo(transaction.loser) }

        if (transaction.tax != null && transaction.taxPercentage != null) {
            // Taxed earning
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.CoinFlipBet.WonTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            loserTag = "${loserUserInfo?.name?.replace("`", "")}#${loserUserInfo?.discriminator}",
                            loserId = transaction.loser.value
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.CoinFlipBet.LostTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            winnerTag = "${winnerUserInfo?.name?.replace("`", "")}#${winnerUserInfo?.discriminator}",
                            winnerId = transaction.winner.value
                        )
                    )
                )
            }
        } else {
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.CoinFlipBet.Won(
                            quantityAfterTax = transaction.quantity,
                            loserTag = "${loserUserInfo?.name?.replace("`", "")}#${loserUserInfo?.discriminator}",
                            loserId = transaction.loser.value
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.CoinFlipBet.Lost(
                            quantity = transaction.quantity,
                            winnerTag = "${winnerUserInfo?.name?.replace("`", "")}#${winnerUserInfo?.discriminator}",
                            winnerId = transaction.winner.value
                        )
                    )
                )
            }
        }
    }
}