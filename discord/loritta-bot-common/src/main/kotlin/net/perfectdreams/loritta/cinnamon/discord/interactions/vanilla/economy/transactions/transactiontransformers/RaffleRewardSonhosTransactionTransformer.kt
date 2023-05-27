package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers.DivineInterventionSonhosTransactionTransformer.appendMoneyEarnedEmoji
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.EmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.RaffleRewardSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object RaffleRewardSonhosTransactionTransformer : SonhosTransactionTransformer<RaffleRewardSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: RaffleRewardSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyEarnedEmoji()

        if (transaction.tax != null && transaction.taxPercentage != null) {
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Raffle.WonRaffleTaxed(transaction.quantityAfterTax, transaction.quantity)
                )
            )
        } else {
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Raffle.WonRaffle(transaction.quantity)
                )
            )
        }
    }
}