package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.PaymentSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object PaymentSonhosTransactionTransformer : SonhosTransactionTransformer<PaymentSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: PaymentSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val receivedTheSonhos = transaction.user == transaction.receivedBy
        val receiverUserInfo = cachedUserInfos.getOrPut(transaction.receivedBy) { loritta.getCachedUserInfo(transaction.receivedBy) }
        val giverUserInfo = cachedUserInfos.getOrPut(transaction.givenBy) { loritta.getCachedUserInfo(transaction.givenBy) }

        if (receivedTheSonhos) {
            appendMoneyEarnedEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.Received(transaction.sonhos, "${giverUserInfo?.name?.stripCodeBackticks()}#${giverUserInfo?.discriminator}", transaction.givenBy.value)
                )
            )
        } else {
            appendMoneyLostEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.Sent(transaction.sonhos, "${receiverUserInfo?.name?.stripCodeBackticks()}#${receiverUserInfo?.discriminator}", transaction.receivedBy.value)
                )
            )
        }
    }
}