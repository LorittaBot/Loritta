package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.*

object SimpleSonhosTransactionTransformers {
    val BlackjackDoubleDownTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackDoubleDownTransaction>(false) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.DoubleDown(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackRefundTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackRefundTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Refunded(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackInsuranceTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackInsuranceTransaction>(false) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Insurance(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackInsurancePayoutTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackInsurancePayoutTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.InsurancePayout(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackTiedTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackTiedTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Tied(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackJoinedTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackJoinedTransaction>(false) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Joined(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackSplitTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackSplitTransaction>(false) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Split(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackPayoutTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackPayoutTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Payout(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    private fun formatGuildInfo(guildInfo: DropGuildInfo): String {
        val inviteUrl = guildInfo.guildInviteId
        return if (inviteUrl != null) {
            "`${guildInfo.guildName}` [`discord.gg/$inviteUrl`]"
        } else {
            "`${guildInfo.guildName}`"
        }
    }

    val DropChatTransformer = SimpleSonhosTransactionTransformer<DropChatTransaction> { transformerInstance, loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        val givenById = transaction.givenById
        if (givenById != null) {
            val giverUserInfo = cachedUserInfos.getOrPut(UserId(givenById)) { loritta.lorittaShards.retrieveUserInfoById(givenById) }
            val receiverUserInfo = cachedUserInfos.getOrPut(UserId(transaction.receivedById)) { loritta.lorittaShards.retrieveUserInfoById(transaction.receivedById) }

            if (transaction.charged) {
                with(transformerInstance) {
                    appendMoneyLostEmoji()
                }

                val guildInfo = transaction.guildInfo
                if (guildInfo != null) {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatSentWithGuildInformation(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, receiverUserInfo?.name, receiverUserInfo?.globalName, receiverUserInfo?.discriminator),
                                formatGuildInfo(guildInfo),
                                transaction.guildId.toString()
                            )
                        )
                    )
                } else {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatSent(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, receiverUserInfo?.name, receiverUserInfo?.globalName, receiverUserInfo?.discriminator),
                                transaction.guildId.toString()
                            )
                        )
                    )
                }
            } else {
                with(transformerInstance) {
                    appendMoneyEarnedEmoji()
                }

                val guildInfo = transaction.guildInfo
                if (guildInfo != null) {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatReceivedWithGuildInformation(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, giverUserInfo?.name, giverUserInfo?.globalName, giverUserInfo?.discriminator),
                                formatGuildInfo(guildInfo),
                                transaction.guildId.toString()
                            )
                        )
                    )
                } else {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatReceived(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, giverUserInfo?.name, giverUserInfo?.globalName, giverUserInfo?.discriminator),
                                transaction.guildId.toString()
                            )
                        )
                    )
                }
            }
        } else {
            with(transformerInstance) {
                appendMoneyEarnedEmoji()
            }

            val guildInfo = transaction.guildInfo
            if (guildInfo != null) {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatReceivedWithGuildInformationAdmin(
                            transaction.sonhos,
                            formatGuildInfo(guildInfo),
                            transaction.guildId.toString()
                        )
                    )
                )
            } else {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.ChatReceivedAdmin(
                            transaction.sonhos,
                            transaction.guildId.toString()
                        )
                    )
                )
            }
        }
    }

    val DropCallTransformer = SimpleSonhosTransactionTransformer<DropCallTransaction> { transformerInstance, loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        val givenById = transaction.givenById
        if (givenById != null) {
            val giverUserInfo = cachedUserInfos.getOrPut(UserId(givenById)) { loritta.lorittaShards.retrieveUserInfoById(givenById) }
            val receiverUserInfo = cachedUserInfos.getOrPut(UserId(transaction.receivedById)) { loritta.lorittaShards.retrieveUserInfoById(transaction.receivedById) }

            if (transaction.charged) {
                with(transformerInstance) {
                    appendMoneyLostEmoji()
                }

                val guildInfo = transaction.guildInfo
                if (guildInfo != null) {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallSentWithGuildInformation(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, receiverUserInfo?.name, receiverUserInfo?.globalName, receiverUserInfo?.discriminator),
                                formatGuildInfo(guildInfo),
                                transaction.guildId.toString()
                            )
                        )
                    )
                } else {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallSent(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, receiverUserInfo?.name, receiverUserInfo?.globalName, receiverUserInfo?.discriminator),
                                transaction.guildId.toString()
                            )
                        )
                    )
                }
            } else {
                with(transformerInstance) {
                    appendMoneyEarnedEmoji()
                }

                val guildInfo = transaction.guildInfo
                if (guildInfo != null) {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallReceivedWithGuildInformation(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, giverUserInfo?.name, giverUserInfo?.globalName, giverUserInfo?.discriminator),
                                formatGuildInfo(guildInfo),
                                transaction.guildId.toString()
                            )
                        )
                    )
                } else {
                    append(
                        i18nContext.get(
                            SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallReceived(
                                transaction.sonhos,
                                convertToUserNameCodeBlockPreviewTag(transaction.receivedById, giverUserInfo?.name, giverUserInfo?.globalName, giverUserInfo?.discriminator),
                                transaction.guildId.toString()
                            )
                        )
                    )
                }
            }
        } else {
            with(transformerInstance) {
                appendMoneyEarnedEmoji()
            }

            val guildInfo = transaction.guildInfo
            if (guildInfo != null) {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallReceivedWithGuildInformationAdmin(
                            transaction.sonhos,
                            formatGuildInfo(guildInfo),
                            transaction.guildId.toString()
                        )
                    )
                )
            } else {
                append(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Drop.CallReceivedAdmin(
                            transaction.sonhos,
                            transaction.guildId.toString()
                        )
                    )
                )
            }
        }
    }

    val MinesJoinedTransactionTransformer = SimpleSonhosTransactionTransformer<MinesJoinedTransaction>(false) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Mines.Joined(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val MinesPayoutTransactionTransformer = SimpleSonhosTransactionTransformer<MinesPayoutTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Mines.Payout(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val MinesRefundTransactionTransformer = SimpleSonhosTransactionTransformer<MinesRefundTransaction>(true) { _, _, i18nContext, cachedUserInfo, cachedUserInfos, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Mines.Refunded(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }
}