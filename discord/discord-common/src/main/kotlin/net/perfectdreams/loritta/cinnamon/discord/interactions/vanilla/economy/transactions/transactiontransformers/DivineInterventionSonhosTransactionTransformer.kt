package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.DivineInterventionSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object DivineInterventionSonhosTransactionTransformer : SonhosTransactionTransformer<DivineInterventionSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: DivineInterventionSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        when (transaction.action) {
            DivineInterventionTransactionEntryAction.ADDED_SONHOS -> {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.DivineIntervention.Received(transaction.sonhos)
                    )
                )
            }
            DivineInterventionTransactionEntryAction.REMOVED_SONHOS -> {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.DivineIntervention.Lost(transaction.sonhos)
                    )
                )
            }
        }
    }
}