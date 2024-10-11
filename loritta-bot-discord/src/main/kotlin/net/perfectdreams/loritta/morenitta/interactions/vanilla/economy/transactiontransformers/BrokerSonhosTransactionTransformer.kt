package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.BrokerSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object BrokerSonhosTransactionTransformer : SonhosTransactionTransformer<BrokerSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: BrokerSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        when (transaction.action) {
            LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.BOUGHT_SHARES -> {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.HomeBroker.BoughtShares(
                            transaction.stockQuantity,
                            transaction.ticker,
                            transaction.sonhos
                        )
                    )
                )
            }

            LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.SOLD_SHARES -> {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.HomeBroker.SoldShares(
                            transaction.stockQuantity,
                            transaction.ticker,
                            transaction.sonhos
                        )
                    )
                )
            }
        }
    }
}