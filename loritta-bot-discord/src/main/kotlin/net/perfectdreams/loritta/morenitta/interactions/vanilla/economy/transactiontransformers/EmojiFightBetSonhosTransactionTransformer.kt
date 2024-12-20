package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.EmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object EmojiFightBetSonhosTransactionTransformer : SonhosTransactionTransformer<EmojiFightBetSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: EmojiFightBetSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val wonTheBet = transaction.user == transaction.winner
        val winnerUserInfo =
            cachedUserInfos.getOrPut(transaction.winner) {
                KotlinLogging.logger {}.info { "EmojiFightBetSonhosTransactionTransformer#retrieveUserInfoById - UserId: ${transaction.winner}" }

                loritta.lorittaShards.retrieveUserInfoById(transaction.winner)
            }
        // We don't store the loser because there may be multiple losers, so we only check that if the user isn't the "winner", then they are the loser
        val loserUserInfo = cachedUserInfo.id

        val userCountExcludingTheWinner = transaction.usersInMatch - 1

        if (transaction.tax != null && transaction.taxPercentage != null) {
            // Taxed earning
            if (wonTheBet) {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.EmojiFightBet.WonTaxed(
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
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.EmojiFightBet.LostTaxed(
                            quantity = transaction.entryPrice,
                            quantityAfterTax = transaction.entryPriceAfterTax,
                            winnerUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.winner.value.toLong(),
                                winnerUserInfo?.name,
                                winnerUserInfo?.globalName,
                                winnerUserInfo?.discriminator,
                            ),
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
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.EmojiFightBet.Won(
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
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.EmojiFightBet.Lost(
                            quantity = transaction.entryPrice,
                            winnerUserPreview = convertToUserNameCodeBlockPreviewTag(
                                transaction.winner.value.toLong(),
                                winnerUserInfo?.name,
                                winnerUserInfo?.globalName,
                                winnerUserInfo?.discriminator,
                            ),
                            emojiFightEmoji = transaction.emoji
                        )
                    )
                )
            }
        }
    }
}