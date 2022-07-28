package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object BrokerSonhosTransactionTransformer : SonhosTransactionTransformer<BrokerSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
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
                        TransactionsCommand.I18N_PREFIX.Types.HomeBroker.BoughtShares(
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
                        TransactionsCommand.I18N_PREFIX.Types.HomeBroker.SoldShares(
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