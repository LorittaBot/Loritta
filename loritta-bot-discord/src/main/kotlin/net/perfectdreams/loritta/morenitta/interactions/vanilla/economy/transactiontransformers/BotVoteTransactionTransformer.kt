package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.WebsiteVoteSource
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.BotVoteSonhosTransaction
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId

object BotVoteTransactionTransformer : SonhosTransactionTransformer<BotVoteSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: BotVoteSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        when (transaction.websiteSource) {
            WebsiteVoteSource.TOP_GG -> {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.BotVote.TopGg(transaction.sonhos)
                    )
                )
            }
        }
    }
}