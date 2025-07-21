package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.I18nContextUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ThirdPartyPaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.ThirdPartySonhosTransferRequests
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.ThirdPartySonhosTransferUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredThirdPartyPaymentSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import java.time.Instant

class ThirdPartySonhosTransferInteractionsListener(val loritta: LorittaBot) : ListenerAdapter() {
    // This code is based off SonhosTransferInteractionsListener
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild

        if (event.componentId.startsWith(ThirdPartySonhosTransferUtils.THIRD_PARTY_SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX + ":")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                // WE DO NOT DEFER THE MESSAGE BECAUSE THAT WILL HORRIBLE ISSUES!!!
                // Horrible issues = users SPAMMING the message causing SHARED RATE LIMITS that cannot be properly ratelimited by nirn-proxy or JDA because THERE ISN'T ANYTHING ON THE PATH
                // TO LET THE LIBRARY WAIT THE REQUEST
                val i18nContext = if (guild != null) {
                    val serverConfig = loritta.getOrCreateServerConfig(guild.idLong, true)
                    loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
                } else {
                    I18nContextUtils.convertDiscordLocaleToI18nContext(loritta.languageManager, event.interaction.userLocale) ?: loritta.languageManager.defaultI18nContext
                }

                val result = loritta.transaction {
                    val now = Instant.now()

                    val sonhosTransferRequestData = ThirdPartySonhosTransferRequests.selectAll()
                        .where {
                            ThirdPartySonhosTransferRequests.id eq dbId
                        }
                        // Lock the rows for update to avoid any parallel executions causing issues
                        .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate())
                        .firstOrNull()

                    if (sonhosTransferRequestData == null)
                        return@transaction TransferResult.UnknownRequest

                    val isGiver = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.giver] == event.user.idLong
                    val isReceiver = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.receiver] == event.user.idLong
                    var giverHasAccepted = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.giverAcceptedAt] != null
                    var receiverHasAccepted = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.receiverAcceptedAt] != null

                    val isInvolvedInRequest = isGiver || isReceiver
                    if (!isInvolvedInRequest)
                        return@transaction TransferResult.NotTheUser

                    if (sonhosTransferRequestData[ThirdPartySonhosTransferRequests.transferredAt] != null)
                        return@transaction TransferResult.AlreadyTransferred

                    if (now > sonhosTransferRequestData[ThirdPartySonhosTransferRequests.expiresAt])
                        return@transaction TransferResult.RequestExpired

                    if (isGiver) {
                        if (giverHasAccepted) {
                            ThirdPartySonhosTransferRequests.update({ ThirdPartySonhosTransferRequests.id eq dbId }) {
                                it[ThirdPartySonhosTransferRequests.giverAcceptedAt] = null
                            }
                            giverHasAccepted = false
                        } else {
                            ThirdPartySonhosTransferRequests.update({ ThirdPartySonhosTransferRequests.id eq dbId }) {
                                it[ThirdPartySonhosTransferRequests.giverAcceptedAt] = now
                            }
                            giverHasAccepted = true
                        }
                    } else {
                        if (receiverHasAccepted) {
                            ThirdPartySonhosTransferRequests.update({ ThirdPartySonhosTransferRequests.id eq dbId }) {
                                it[ThirdPartySonhosTransferRequests.receiverAcceptedAt] = null
                            }
                            receiverHasAccepted = false
                        } else {
                            ThirdPartySonhosTransferRequests.update({ ThirdPartySonhosTransferRequests.id eq dbId }) {
                                it[ThirdPartySonhosTransferRequests.receiverAcceptedAt] = now
                            }
                            receiverHasAccepted = true
                        }
                    }

                    if (giverHasAccepted && receiverHasAccepted) {
                        val howMuch = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.quantity]
                        val howMuchShouldBeRemoved = howMuch
                        val howMuchShouldBeAdded = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.quantity] - sonhosTransferRequestData[ThirdPartySonhosTransferRequests.tax]

                        val receiverProfile = loritta.pudding.users.getOrCreateUserProfile(
                            net.perfectdreams.loritta.serializable.UserId(sonhosTransferRequestData[ThirdPartySonhosTransferRequests.receiver])
                        )
                        val giverProfile = loritta.pudding.users.getOrCreateUserProfile(
                            net.perfectdreams.loritta.serializable.UserId(sonhosTransferRequestData[ThirdPartySonhosTransferRequests.giver])
                        )

                        if (howMuchShouldBeRemoved > giverProfile.money)
                            return@transaction TransferResult.NotEnoughSonhos // get tf outta here

                        // Update the sonhos of both users
                        Profiles.update({ Profiles.id eq receiverProfile.id.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it[Profiles.money] = Profiles.money + howMuchShouldBeAdded
                            }
                        }

                        Profiles.update({ Profiles.id eq giverProfile.id.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it[Profiles.money] = Profiles.money - howMuchShouldBeRemoved
                            }
                        }

                        // Insert transactions about it
                        // Cinnamon transaction log
                        val paymentResult = ThirdPartyPaymentSonhosTransactionResults.insertAndGetId {
                            it[ThirdPartyPaymentSonhosTransactionResults.tokenUser] = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.tokenUser]
                            it[ThirdPartyPaymentSonhosTransactionResults.givenBy] = giverProfile.id.value.toLong()
                            it[ThirdPartyPaymentSonhosTransactionResults.receivedBy] = receiverProfile.id.value.toLong()
                            it[ThirdPartyPaymentSonhosTransactionResults.reason] = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.reason]
                            it[ThirdPartyPaymentSonhosTransactionResults.tax] = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.tax]
                            it[ThirdPartyPaymentSonhosTransactionResults.taxPercentage] = sonhosTransferRequestData[ThirdPartySonhosTransferRequests.taxPercentage]
                            it[ThirdPartyPaymentSonhosTransactionResults.sonhos] = howMuch
                            it[ThirdPartyPaymentSonhosTransactionResults.timestamp] = now
                        }

                        val storedTransaction = StoredThirdPartyPaymentSonhosTransaction(paymentResult.value)

                        SimpleSonhosTransactionsLogUtils.insert(
                            receiverProfile.id.value.toLong(),
                            now,
                            TransactionType.PAYMENT,
                            howMuchShouldBeAdded,
                            storedTransaction
                        )

                        SimpleSonhosTransactionsLogUtils.insert(
                            giverProfile.id.value.toLong(),
                            now,
                            TransactionType.PAYMENT,
                            howMuchShouldBeRemoved,
                            storedTransaction
                        )

                        ThirdPartySonhosTransferRequests.update({ ThirdPartySonhosTransferRequests.id eq dbId }) {
                            it[ThirdPartySonhosTransferRequests.transferredAt] = now
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
                            sonhosTransferRequestData[ThirdPartySonhosTransferRequests.receiver],
                            sonhosTransferRequestData[ThirdPartySonhosTransferRequests.giver],
                            howMuch,
                            sonhosTransferRequestData[ThirdPartySonhosTransferRequests.tax],
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
                        val hook = event.editMessage(
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

                        hook.sendMessage(
                            MessageCreate {
                                mentions {
                                    // Allow mentioning the giver AND the receiver!
                                    user(result.giverId)
                                    user(result.receiverId)
                                }

                                if (result.tax != 0L) {
                                    styled(
                                        i18nContext.get(
                                            SonhosCommand.PAY_I18N_PREFIX.ThirdPartySuccessfullyTransferred(
                                                "<@${result.receiverId}>",
                                                result.howMuch - result.tax,
                                                result.tax
                                            )
                                        ),
                                        Emotes.Handshake
                                    )
                                } else {
                                    styled(
                                        i18nContext.get(
                                            SonhosCommand.PAY_I18N_PREFIX.ThirdPartySuccessfullyTransferredNoTax(
                                                "<@${result.receiverId}>",
                                                result.howMuch
                                            )
                                        ),
                                        Emotes.Handshake
                                    )
                                }

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
                        event.editMessage(
                            MessageEdit {
                                actionRow(
                                    Button.of(
                                        ButtonStyle.PRIMARY,
                                        "${ThirdPartySonhosTransferUtils.THIRD_PARTY_SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX}:${dbId}",
                                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.AcceptTransfer(result.quantityApproved)),
                                        Emotes.Handshake.toJDA()
                                    )
                                )
                            }
                        ).setReplace(false).await()
                    }
                    TransferResult.AlreadyTransferred -> {
                        event.reply(
                            MessageCreate {
                                styled(
                                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.ThisTransactionHasAlreadyBeenTransferred),
                                    Emotes.LoriSob
                                )
                            }
                        ).setEphemeral(true).await()
                    }
                    TransferResult.NotEnoughSonhos -> {
                        event.editMessage(
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
                        event.reply(
                            MessageCreate {
                                styled(
                                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.YouArentInvolvedInThisTransaction),
                                    Emotes.LoriBonk
                                )
                            }
                        ).setEphemeral(true).await()
                    }
                    TransferResult.RequestExpired -> {
                        event.editMessage(
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
                        event.editMessage(
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
            val tax: Long,
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