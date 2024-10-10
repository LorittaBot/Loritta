package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.LoriCoolCardsPaymentSonhosTradeTransaction
import net.perfectdreams.loritta.serializable.UserId

object LoriCoolCardsPaymentSonhosTradeTransactionTransformer : SonhosTransactionTransformer<LoriCoolCardsPaymentSonhosTradeTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: LoriCoolCardsPaymentSonhosTradeTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val receivedTheSonhos = transaction.user == transaction.receivedBy
        val receiverUserInfo = cachedUserInfos.getOrPut(transaction.receivedBy) { loritta.getCachedUserInfo(transaction.receivedBy) }
        val giverUserInfo = cachedUserInfos.getOrPut(transaction.givenBy) { loritta.getCachedUserInfo(transaction.givenBy) }

        if (receivedTheSonhos) {
            appendMoneyEarnedEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Loricoolcards.Received(transaction.sonhos, "${giverUserInfo?.name?.stripCodeBackticks()}#${giverUserInfo?.discriminator}", transaction.givenBy.value)
                )
            )
        } else {
            appendMoneyLostEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Loricoolcards.Sent(transaction.sonhos, "${receiverUserInfo?.name?.stripCodeBackticks()}#${receiverUserInfo?.discriminator}", transaction.receivedBy.value)
                )
            )
        }
    }
}