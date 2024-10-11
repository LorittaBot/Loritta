package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransferRequests
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor.Companion.SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredPaymentSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import java.time.Instant

class SonhosTransferInteractionsListener(val loritta: LorittaBot) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith(SonhosPayExecutor.SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX + ":")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val deferredReply = event.interaction.deferEdit().await()

                val serverConfig = loritta.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val result = loritta.transaction {
                    val now = Instant.now()

                    val sonhosTransferRequestData = SonhosTransferRequests.selectAll()
                        .where {
                            SonhosTransferRequests.id eq dbId
                        }
                        // Lock the rows for update to avoid any parallel executions causing issues
                        .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate())
                        .firstOrNull()

                    if (sonhosTransferRequestData == null)
                        return@transaction TransferResult.UnknownRequest

                    val isGiver = sonhosTransferRequestData[SonhosTransferRequests.giver] == event.user.idLong
                    val isReceiver = sonhosTransferRequestData[SonhosTransferRequests.receiver] == event.user.idLong
                    var giverHasAccepted = sonhosTransferRequestData[SonhosTransferRequests.giverAcceptedAt] != null
                    var receiverHasAccepted = sonhosTransferRequestData[SonhosTransferRequests.receiverAcceptedAt] != null

                    val isInvolvedInRequest = isGiver || isReceiver
                    if (!isInvolvedInRequest)
                        return@transaction TransferResult.NotTheUser

                    if (sonhosTransferRequestData[SonhosTransferRequests.transferredAt] != null)
                        return@transaction TransferResult.AlreadyTransferred

                    if (now > sonhosTransferRequestData[SonhosTransferRequests.expiresAt])
                        return@transaction TransferResult.RequestExpired

                    if (isGiver) {
                        if (giverHasAccepted) {
                            SonhosTransferRequests.update({ SonhosTransferRequests.id eq dbId }) {
                                it[SonhosTransferRequests.giverAcceptedAt] = null
                            }
                            giverHasAccepted = false
                        } else {
                            SonhosTransferRequests.update({ SonhosTransferRequests.id eq dbId }) {
                                it[SonhosTransferRequests.giverAcceptedAt] = now
                            }
                            giverHasAccepted = true
                        }
                    } else {
                        if (receiverHasAccepted) {
                            SonhosTransferRequests.update({ SonhosTransferRequests.id eq dbId }) {
                                it[SonhosTransferRequests.receiverAcceptedAt] = null
                            }
                            receiverHasAccepted = false
                        } else {
                            SonhosTransferRequests.update({ SonhosTransferRequests.id eq dbId }) {
                                it[SonhosTransferRequests.receiverAcceptedAt] = now
                            }
                            receiverHasAccepted = true
                        }
                    }

                    if (giverHasAccepted && receiverHasAccepted) {
                        val howMuch = sonhosTransferRequestData[SonhosTransferRequests.quantity]

                        val receiverProfile = loritta.pudding.users.getOrCreateUserProfile(
                            net.perfectdreams.loritta.serializable.UserId(sonhosTransferRequestData[SonhosTransferRequests.receiver])
                        )
                        val giverProfile = loritta.pudding.users.getOrCreateUserProfile(
                            net.perfectdreams.loritta.serializable.UserId(sonhosTransferRequestData[SonhosTransferRequests.giver])
                        )

                        if (howMuch > giverProfile.money)
                            return@transaction TransferResult.NotEnoughSonhos // get tf outta here

                        // Update the sonhos of both users
                        Profiles.update({ Profiles.id eq receiverProfile.id.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it[Profiles.money] = Profiles.money + howMuch
                            }
                        }

                        Profiles.update({ Profiles.id eq giverProfile.id.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it[Profiles.money] = Profiles.money - howMuch
                            }
                        }

                        // Insert transactions about it
                        // Cinnamon transaction log
                        val paymentResult = PaymentSonhosTransactionResults.insertAndGetId {
                            it[PaymentSonhosTransactionResults.givenBy] = giverProfile.id.value.toLong()
                            it[PaymentSonhosTransactionResults.receivedBy] = receiverProfile.id.value.toLong()
                            it[PaymentSonhosTransactionResults.sonhos] = howMuch
                            it[PaymentSonhosTransactionResults.timestamp] = now
                        }

                        SimpleSonhosTransactionsLogUtils.insert(
                            receiverProfile.id.value.toLong(),
                            now,
                            TransactionType.PAYMENT,
                            howMuch,
                            StoredPaymentSonhosTransaction(
                                giverProfile.id.value.toLong(),
                                receiverProfile.id.value.toLong(),
                                paymentResult.value
                            )
                        )

                        SimpleSonhosTransactionsLogUtils.insert(
                            giverProfile.id.value.toLong(),
                            now,
                            TransactionType.PAYMENT,
                            howMuch,
                            StoredPaymentSonhosTransaction(
                                giverProfile.id.value.toLong(),
                                receiverProfile.id.value.toLong(),
                                paymentResult.value
                            )
                        )

                        SonhosTransferRequests.update({ SonhosTransferRequests.id eq dbId }) {
                            it[SonhosTransferRequests.transferredAt] = now
                        }

                        // Get the profiles again
                        val updatedReceiverProfile = loritta.pudding.users.getOrCreateUserProfile(receiverProfile.id)
                        val updatedGiverProfile = loritta.pudding.users.getOrCreateUserProfile(giverProfile.id)

                        val receiverRanking = if (updatedReceiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(
                            updatedReceiverProfile.money
                        ) else null
                        val giverRanking = if (updatedGiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(
                            updatedGiverProfile.money
                        ) else null

                        return@transaction TransferResult.Success(
                            sonhosTransferRequestData[SonhosTransferRequests.receiver],
                            sonhosTransferRequestData[SonhosTransferRequests.giver],
                            howMuch,
                            updatedReceiverProfile.money,
                            receiverRanking,
                            updatedGiverProfile.money,
                            giverRanking
                        )
                    } else {
                        var idx = 0
                        if (giverHasAccepted)
                            idx++
                        if (receiverHasAccepted)
                            idx++
                        return@transaction TransferResult.RequestUpdated(idx)
                    }
                }

                when (result) {
                    is TransferResult.Success -> {
                        // Let's go!!
                        deferredReply.editOriginal(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.SUCCESS,
                                        "dummy",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferAccepted),
                                        Emotes.LoriCard.toJDA()
                                    ).asDisabled()
                                )
                            }
                        ).setReplace(false).await()

                        deferredReply.sendMessage(
                            MessageCreate {
                                styled(
                                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.SuccessfullyTransferred("<@${result.receiverId}>", result.howMuch)),
                                    Emotes.Handshake
                                )

                                // Let's add a random emoji just to look cute
                                val user1Emote = SonhosUtils.HANGLOOSE_EMOTES.random()
                                val user2Emote = SonhosUtils.HANGLOOSE_EMOTES.filter { it != user1Emote }.random()

                                if (result.giverRanking != null) {
                                    styled(
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking("<@${result.giverId}>", SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity), result.giverQuantity, result.giverRanking)),
                                        user1Emote
                                    )
                                } else {
                                    styled(
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos("<@${result.giverId}>", SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity), result.giverQuantity)),
                                        user1Emote
                                    )
                                }
                                if (result.receiverRanking != null) {
                                    styled(
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking("<@${result.receiverId}>", SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity), result.receiverQuantity, result.receiverRanking)),
                                        user2Emote
                                    )
                                } else {
                                    styled(
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos("<@${result.receiverId}>", SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity), result.receiverQuantity)),
                                        user2Emote
                                    )
                                }
                            }
                        ).await()
                    }
                    is TransferResult.RequestUpdated -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.PRIMARY,
                                        "$SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX:${dbId}",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.AcceptTransfer(result.quantityApproved)),
                                        Emotes.Handshake.toJDA()
                                    )
                                )
                            }
                        ).setReplace(false).await()
                    }
                    TransferResult.AlreadyTransferred -> {
                        deferredReply.sendMessage(
                            MessageCreate {
                                styled(
                                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.ThisTransactionHasAlreadyBeenTransferred),
                                    Emotes.LoriSob
                                )
                            }
                        ).setEphemeral(true).await()
                    }
                    TransferResult.NotEnoughSonhos -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.DANGER,
                                        "dummy",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.FailReasons.InsufficientSonhos),
                                        Emotes.LoriSob.toJDA()
                                    ).asDisabled()
                                )
                            }
                        ).setReplace(false).await()
                    }
                    TransferResult.NotTheUser -> {
                        deferredReply.sendMessage(
                            MessageCreate {
                                styled(
                                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.YouArentInvolvedInThisTransaction),
                                    Emotes.LoriBonk
                                )
                            }
                        ).setEphemeral(true).await()
                    }
                    TransferResult.RequestExpired -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.DANGER,
                                        "dummy",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.FailReasons.Expired),
                                        Emotes.LoriSob.toJDA()
                                    ).asDisabled()
                                )
                            }
                        ).setReplace(false).await()
                    }
                    TransferResult.UnknownRequest -> {
                        deferredReply.editOriginal(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.DANGER,
                                        "dummy",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.FailReasons.UnknownRequest),
                                        Emotes.LoriSob.toJDA()
                                    ).asDisabled()
                                )
                            }
                        ).setReplace(false).await()
                    }
                }
            }
        }
    }

    sealed class TransferResult {
        data class Success(
            val receiverId: Long,
            val giverId: Long,
            val howMuch: Long,
            val receiverQuantity: Long,
            val receiverRanking: Long?,
            val giverQuantity: Long,
            val giverRanking: Long?
        ) : TransferResult()
        data class RequestUpdated(val quantityApproved: Int) : TransferResult()
        data object RequestExpired : TransferResult()
        data object NotTheUser : TransferResult()
        data object NotEnoughSonhos : TransferResult()
        data object AlreadyTransferred : TransferResult()
        data object UnknownRequest : TransferResult()
    }
}