package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.LoriCoolCardsBoughtBoosterPackSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object LoriCoolCardsBoughtBoosterPackSonhosTransactionTransformer : SonhosTransactionTransformer<LoriCoolCardsBoughtBoosterPackSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: LoriCoolCardsBoughtBoosterPackSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyLostEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Loricoolcards.BoughtBoosterPack(transaction.sonhos)
            )
        )
    }
}