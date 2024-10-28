package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventsAttributes
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.ReactionEventSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object ReactionEventSonhosTransactionTransformer : SonhosTransactionTransformer<ReactionEventSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: ReactionEventSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val event = ReactionEventsAttributes.attributes[transaction.eventInternalId]!!

        appendMoneyEarnedEmoji()
        append(event.createSonhosRewardTransactionMessage(i18nContext, transaction.sonhos, transaction.craftedCount))
    }
}