package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.CoinFlipBetSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object CoinFlipBetSonhosTransactionTransformer : SonhosTransactionTransformer<CoinFlipBetSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: CoinFlipBetSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val wonTheBet = transaction.user == transaction.winner
        val winnerUserInfo =
            cachedUserInfos.getOrPut(transaction.winner) {
                HarmonyLoggerFactory.logger {}.value.info { "CoinFlipBetSonhosTransactionTransformer#retrieveUserInfoById - UserId: ${transaction.winner}" }

                loritta.lorittaShards.retrieveUserInfoById(transaction.winner)
            }
        val loserUserInfo =
            cachedUserInfos.getOrPut(transaction.loser) {
                HarmonyLoggerFactory.logger {}.value.info { "CoinFlipBetSonhosTransactionTransformer#retrieveUserInfoById - UserId: ${transaction.loser}" }

                loritta.lorittaShards.retrieveUserInfoById(transaction.loser)
            }

        if (transaction.tax != null && transaction.taxPercentage != null) {
            // Taxed earning
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBet.WonTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            loserUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.loser.value.toLong(),
                                loserUserInfo?.name,
                                loserUserInfo?.globalName,
                                loserUserInfo?.discriminator,
                            )
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBet.LostTaxed(
                            quantity = transaction.quantity,
                            quantityAfterTax = transaction.quantityAfterTax,
                            winnerUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.winner.value.toLong(),
                                winnerUserInfo?.name,
                                winnerUserInfo?.globalName,
                                winnerUserInfo?.discriminator,
                            )
                        )
                    )
                )
            }
        } else {
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBet.Won(
                            quantityAfterTax = transaction.quantity,
                            loserUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.loser.value.toLong(),
                                loserUserInfo?.name,
                                loserUserInfo?.globalName,
                                loserUserInfo?.discriminator,
                            )
                        )
                    )
                )
            } else {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.CoinFlipBet.Lost(
                            quantity = transaction.quantity,
                            winnerUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.winner.value.toLong(),
                                winnerUserInfo?.name,
                                winnerUserInfo?.globalName,
                                winnerUserInfo?.discriminator,
                            )
                        )
                    )
                )
            }
        }
    }
}