package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.LorittaItemShopComissionBackgroundTransaction
import net.perfectdreams.loritta.serializable.LorittaItemShopComissionProfileDesignTransaction
import net.perfectdreams.loritta.serializable.UserId

object LorittaItemShopComissionProfileDesignSonhosTransactionTransformer : SonhosTransactionTransformer<LorittaItemShopComissionProfileDesignTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: LorittaItemShopComissionProfileDesignTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyLostEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.LorittaItemShop.ComissionProfileDesign(transaction.sonhos)
            )
        )
    }
}