package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.PaymentSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object PaymentSonhosTransactionTransformer : SonhosTransactionTransformer<PaymentSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: PaymentSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val receivedTheSonhos = transaction.user == transaction.receivedBy
        val receiverUserInfo =
            cachedUserInfos.getOrPut(transaction.receivedBy) { loritta.lorittaShards.retrieveUserInfoById(transaction.receivedBy) }
        val giverUserInfo =
            cachedUserInfos.getOrPut(transaction.givenBy) { loritta.lorittaShards.retrieveUserInfoById(transaction.givenBy) }

        if (receivedTheSonhos) {
            appendMoneyEarnedEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.Received(
                        transaction.sonhos,
                        convertToUserNameCodeBlockPreviewTag(
                            transaction.givenBy.value.toLong(),
                            giverUserInfo?.name,
                            giverUserInfo?.globalName,
                            giverUserInfo?.discriminator
                        )
                    )
                )
            )
        } else {
            appendMoneyLostEmoji()
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.Sent(
                        transaction.sonhos,
                        convertToUserNameCodeBlockPreviewTag(
                            transaction.receivedBy.value.toLong(),
                            receiverUserInfo?.name,
                            receiverUserInfo?.globalName,
                            receiverUserInfo?.discriminator
                        )
                    )
                )
            )
        }
    }
}