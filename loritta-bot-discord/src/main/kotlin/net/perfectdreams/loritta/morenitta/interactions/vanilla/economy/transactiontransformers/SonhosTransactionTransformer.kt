package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.SonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import kotlin.reflect.KFunction

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

    /**
     * Creates a [StringBuilder] block that appends the [transaction] into a [StringBuilder].
     *
     * Used when [transaction] is not typed
     */
    suspend fun transformGeneric(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: SonhosTransaction
    ) = transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction as T)

    fun StringBuilder.appendMoneyLostEmoji() {
        append(Emotes.MoneyWithWings)
        append(" ")
    }

    fun StringBuilder.appendMoneyEarnedEmoji() {
        append(Emotes.DollarBill)
        append(" ")
    }
}

/**
 * A "simple" sonhos transaction transformer for transactions that do not require a lot of checks and weird states
 */
fun <T : SonhosTransaction> SimpleSonhosTransactionTransformer(
    earnedMoney: Boolean,
    block: suspend StringBuilder.(LorittaBot, I18nContext, T) -> (Unit)
): SonhosTransactionTransformer<T> {
    return object: SonhosTransactionTransformer<T> {
        override suspend fun transform(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            cachedUserInfo: CachedUserInfo,
            cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
            transaction: T
        ): suspend StringBuilder.() -> Unit = {
            if (earnedMoney)
                appendMoneyEarnedEmoji()
            else
                appendMoneyLostEmoji()

            block(loritta, i18nContext, transaction)
        }
    }
}