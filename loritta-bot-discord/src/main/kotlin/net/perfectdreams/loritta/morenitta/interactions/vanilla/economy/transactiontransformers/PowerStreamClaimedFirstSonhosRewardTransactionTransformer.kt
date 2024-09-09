package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.PowerStreamClaimedFirstSonhosRewardSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object PowerStreamClaimedFirstSonhosRewardTransactionTransformer : SonhosTransactionTransformer<PowerStreamClaimedFirstSonhosRewardSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: PowerStreamClaimedFirstSonhosRewardSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyEarnedEmoji()
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Powerstream.ClaimedFirstSonhosReward(transaction.sonhos)
            )
        )
    }
}