package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.RaffleTicketsSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object RaffleTicketsSonhosTransactionTransformer : SonhosTransactionTransformer<RaffleTicketsSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: net.perfectdreams.loritta.morenitta.utils.CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, net.perfectdreams.loritta.morenitta.utils.CachedUserInfo?>,
        transaction: RaffleTicketsSonhosTransaction
    ): suspend StringBuilder.() -> Unit = {
        appendMoneyLostEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Raffle.BoughtTickets(
                    transaction.ticketQuantity,
                    transaction.sonhos
                )
            )
        )
    }
}