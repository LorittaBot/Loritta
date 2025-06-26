package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.ChargebackedSonhosBundleTransaction
import net.perfectdreams.loritta.serializable.UserId

object ChargebackedSonhosTransactionTransformer : SonhosTransactionTransformer<ChargebackedSonhosBundleTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: ChargebackedSonhosBundleTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        appendMoneyLostEmoji()

        if (transaction.triggeredByUserId == transaction.user.value.toLong()) {
            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.SonhosBundlePurchase.ChargebackedSelf(
                        transaction.sonhos
                    )
                )
            )
        } else {
            HarmonyLoggerFactory.logger {}.value.info { "ChargebackedSonhosTransactionTransformer#retrieveUserInfoById - UserId: ${transaction.triggeredByUserId}" }
            val marriedWithUserInfo = cachedUserInfos.getOrPut(UserId(transaction.triggeredByUserId)) { loritta.lorittaShards.retrieveUserInfoById(transaction.triggeredByUserId) }

            append(
                i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.SonhosBundlePurchase.ChargebackedOther(
                        transaction.sonhos,
                        convertToUserNameCodeBlockPreviewTag(
                            transaction.triggeredByUserId,
                            marriedWithUserInfo?.name,
                            marriedWithUserInfo?.globalName,
                            marriedWithUserInfo?.discriminator,
                        )
                    )
                )
            )
        }
    }
}