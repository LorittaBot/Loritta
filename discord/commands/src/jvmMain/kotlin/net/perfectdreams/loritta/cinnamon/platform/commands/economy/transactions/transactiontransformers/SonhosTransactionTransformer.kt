package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

interface SonhosTransactionTransformer<T : SonhosTransaction> {
    /**
     * Creates a [StringBuilder] block that appends the [transaction] into a [StringBuilder].
     */
    suspend fun transform(
        loritta: LorittaCinnamon,
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