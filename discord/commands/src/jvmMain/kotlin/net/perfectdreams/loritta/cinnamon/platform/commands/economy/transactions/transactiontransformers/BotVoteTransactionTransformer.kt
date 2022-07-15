package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.WebsiteVoteSource
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BotVoteTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object BotVoteTransactionTransformer : SonhosTransactionTransformer<BotVoteTransaction> {
    override suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: BotVoteTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        when (transaction.websiteSource) {
            WebsiteVoteSource.TOP_GG -> {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        TransactionsCommand.I18N_PREFIX.Types.BotVote.TopGG(transaction.sonhos)
                    )
                )
            }
        }
    }
}