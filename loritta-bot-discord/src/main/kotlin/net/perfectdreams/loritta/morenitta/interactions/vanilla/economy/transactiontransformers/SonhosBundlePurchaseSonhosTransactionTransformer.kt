package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.SonhosBundlePurchaseSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object SonhosBundlePurchaseSonhosTransactionTransformer : SonhosTransactionTransformer<SonhosBundlePurchaseSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: SonhosBundlePurchaseSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyEarnedEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.SonhosBundlePurchase.PurchasedSonhos(
                    transaction.sonhos,
                    Emotes.LoriKiss
                )
            )
        )
    }
}