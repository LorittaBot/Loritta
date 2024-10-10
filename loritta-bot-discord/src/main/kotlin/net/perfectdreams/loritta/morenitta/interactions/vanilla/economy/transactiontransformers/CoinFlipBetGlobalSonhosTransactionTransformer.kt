package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.CoinFlipBetGlobalSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object CoinFlipBetGlobalSonhosTransactionTransformer : SonhosTransactionTransformer<CoinFlipBetGlobalSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: CoinFlipBetGlobalSonhosTransaction
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
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBetGlobal.WonTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            loserTag = "${loserUserInfo?.name?.stripCodeBackticks()}#${loserUserInfo?.discriminator}",
                            loserId = transaction.loser.value
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBetGlobal.LostTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            winnerTag = "${winnerUserInfo?.name?.stripCodeBackticks()}#${winnerUserInfo?.discriminator}",
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
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBetGlobal.Won(
                            quantityAfterTax = transaction.quantity,
                            loserTag = "${loserUserInfo?.name?.stripCodeBackticks()}#${loserUserInfo?.discriminator}",
                            loserId = transaction.loser.value
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBetGlobal.Lost(
                            quantity = transaction.quantity,
                            winnerTag = "${winnerUserInfo?.name?.stripCodeBackticks()}#${winnerUserInfo?.discriminator}",
                            winnerId = transaction.winner.value
                        )
                    )
                )
            }
        }
    }
}