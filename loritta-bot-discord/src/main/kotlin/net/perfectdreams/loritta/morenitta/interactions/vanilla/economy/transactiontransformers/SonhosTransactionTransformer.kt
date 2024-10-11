package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.SonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

interface SonhosTransactionTransformer<T : SonhosTransaction> {
    /**
     * Creates a [StringBuilder] block that appends the [transaction] into a [StringBuilder].
     */
    suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: T
    ): suspend StringBuilder.() -> (Unit)

    fun StringBuilder.appendMoneyLostEmoji() {
        append(Emotes.MoneyWithWings)
        append(" ")
    }

    fun StringBuilder.appendMoneyEarnedEmoji() {
        append(Emotes.DollarBill)
        append(" ")
    }
}