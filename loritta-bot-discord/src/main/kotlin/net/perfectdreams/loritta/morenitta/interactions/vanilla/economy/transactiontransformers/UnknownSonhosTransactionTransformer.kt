package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.UnknownSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object UnknownSonhosTransactionTransformer : SonhosTransactionTransformer<UnknownSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: UnknownSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        append("${Emotes.LoriShrug} Unknown Transaction (Bug?)")
    }
}