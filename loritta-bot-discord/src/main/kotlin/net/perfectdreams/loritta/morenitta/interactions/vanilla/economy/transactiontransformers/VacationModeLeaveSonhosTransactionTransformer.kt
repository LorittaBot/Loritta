package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.serializable.VacationModeLeaveTransaction

object VacationModeLeaveSonhosTransactionTransformer : SonhosTransactionTransformer<VacationModeLeaveTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: VacationModeLeaveTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyLostEmoji()

        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.VacationMode.PaidToRemove(transaction.sonhos)
            )
        )
    }
}