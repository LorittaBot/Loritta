package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.SparklyPowerLSXSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object SparklyPowerLSXSonhosTransactionTransformer : SonhosTransactionTransformer<SparklyPowerLSXSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: SparklyPowerLSXSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        when (transaction.action) {
            SparklyPowerLSXTransactionEntryAction.EXCHANGED_TO_SPARKLYPOWER -> {
                appendMoneyLostEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.SparklyPowerLsx.ExchangedToSparklyPower(
                            transaction.sonhos,
                            transaction.playerName,
                            "mc.sparklypower.net",
                            transaction.sparklyPowerSonhos,
                        )
                    )
                )
            }
            SparklyPowerLSXTransactionEntryAction.EXCHANGED_FROM_SPARKLYPOWER -> {
                appendMoneyEarnedEmoji()
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.SparklyPowerLsx.ExchangedFromSparklyPower(
                            transaction.sparklyPowerSonhos,
                            transaction.playerName,
                            "mc.sparklypower.net",
                            transaction.sonhos,
                        )
                    )
                )
            }
        }
    }
}