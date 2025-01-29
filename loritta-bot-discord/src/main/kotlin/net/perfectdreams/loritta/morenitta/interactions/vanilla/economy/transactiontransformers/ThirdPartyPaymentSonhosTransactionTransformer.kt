package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.serializable.ThirdPartyPaymentSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId

object ThirdPartyPaymentSonhosTransactionTransformer : SonhosTransactionTransformer<ThirdPartyPaymentSonhosTransaction> {
    override suspend fun transform(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        cachedUserInfo: CachedUserInfo,
        cachedUserInfos: MutableMap<UserId, CachedUserInfo?>,
        transaction: ThirdPartyPaymentSonhosTransaction
    ): suspend StringBuilder.() -> (Unit) = {
        val receivedTheSonhos = transaction.user == transaction.receivedBy
        val receiverUserInfo =
            cachedUserInfos.getOrPut(transaction.receivedBy) {
                KotlinLogging.logger {}.info { "ThirdPartyPaymentSonhosTransaction#retrieveUserInfoById - UserId: ${transaction.receivedBy}" }

                loritta.lorittaShards.retrieveUserInfoById(transaction.receivedBy)
            }
        val giverUserInfo =
            cachedUserInfos.getOrPut(transaction.givenBy) {
                KotlinLogging.logger {}.info { "ThirdPartyPaymentSonhosTransaction#retrieveUserInfoById - UserId: ${transaction.givenBy}" }

                loritta.lorittaShards.retrieveUserInfoById(transaction.givenBy)
            }

        val hasTax = transaction.taxPercentage != 0.0

        if (receivedTheSonhos) {
            appendMoneyEarnedEmoji()

            if (hasTax) {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.ThirdPartyReceived(
                            transaction.sonhos + transaction.tax,
                            transaction.sonhos,
                            convertToUserNameCodeBlockPreviewTag(
                                transaction.givenBy.value.toLong(),
                                giverUserInfo?.name,
                                giverUserInfo?.globalName,
                                giverUserInfo?.discriminator
                            ),
                            transaction.reason.stripCodeMarks()
                        )
                    )
                )
            } else {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.ThirdPartyReceivedNoTax(
                            transaction.sonhos,
                            convertToUserNameCodeBlockPreviewTag(
                                transaction.givenBy.value.toLong(),
                                giverUserInfo?.name,
                                giverUserInfo?.globalName,
                                giverUserInfo?.discriminator
                            ),
                            transaction.reason.stripCodeMarks()
                        )
                    )
                )
            }
        } else {
            appendMoneyLostEmoji()

            if (hasTax) {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.ThirdPartySent(
                            transaction.sonhos + transaction.tax,
                            transaction.sonhos,
                            convertToUserNameCodeBlockPreviewTag(
                                transaction.receivedBy.value.toLong(),
                                receiverUserInfo?.name,
                                receiverUserInfo?.globalName,
                                receiverUserInfo?.discriminator
                            ),
                            transaction.reason.stripCodeMarks()
                        )
                    )
                )
            } else {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Payment.ThirdPartySentNoTax(
                            transaction.sonhos + transaction.tax,
                            convertToUserNameCodeBlockPreviewTag(
                                transaction.receivedBy.value.toLong(),
                                receiverUserInfo?.name,
                                receiverUserInfo?.globalName,
                                receiverUserInfo?.discriminator
                            ),
                            transaction.reason.stripCodeMarks()
                        )
                    )
                )
            }
        }
    }
}