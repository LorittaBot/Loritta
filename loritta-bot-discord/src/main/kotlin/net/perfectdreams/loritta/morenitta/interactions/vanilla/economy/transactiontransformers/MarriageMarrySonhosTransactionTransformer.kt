package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.MarriageMarryTransaction
import net.perfectdreams.loritta.serializable.UserId

object MarriageMarrySonhosTransactionTransformer : SonhosTransactionTransformer<MarriageMarryTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: MarriageMarryTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val marriedWith = cachedUserInfos.getOrPut(UserId(transaction.marriedWithUserId)) { loritta.lorittaShards.retrieveUserInfoById(transaction.marriedWithUserId) }

        appendMoneyLostEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Marriage.Married(transaction.sonhos, "${marriedWith?.name?.stripCodeBackticks()}#${marriedWith?.discriminator}", transaction.marriedWithUserId.toString())
            )
        )
    }
}