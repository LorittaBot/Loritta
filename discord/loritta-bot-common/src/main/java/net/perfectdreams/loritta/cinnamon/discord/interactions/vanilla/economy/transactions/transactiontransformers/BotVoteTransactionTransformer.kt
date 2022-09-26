package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.utils.WebsiteVoteSource
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BotVoteSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object BotVoteTransactionTransformer : SonhosTransactionTransformer<BotVoteSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
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