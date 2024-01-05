package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.interactions.ComponentContextHighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.HighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.editMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.StoredGenericInteractionData
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.StoredPaymentSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

class TransferSonhosButtonExecutor(
    loritta: LorittaBot
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.TRANSFER_SONHOS_BUTTON_EXECUTOR) {
        suspend fun acceptSonhos(
            loritta: LorittaBot,
            context: InteractionContext,
            acceptedUserId: Snowflake,
            transactionRequestMessage: HighLevelEditableMessage,
            decodedGenericInteractionData: StoredGenericInteractionData
        ) {
            val result = loritta.pudding.transaction {
                val dataFromDatabase = loritta.pudding.interactionsData.getInteractionData(decodedGenericInteractionData.interactionDataId) ?: return@transaction Result.DataIsNotPresent // Data is not present! Maybe it expired or it was already processed

                val decoded = Json.decodeFromJsonElement<TransferSonhosData>(dataFromDatabase)
                if (decoded.userId != acceptedUserId)
                    return@transaction Result.NotTheUser(decoded.userId)

                val howMuch = decoded.quantity
                val now = Clock.System.now()

                val receiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.userId))
                val giverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.sourceId))

                if (decoded.quantity > giverProfile.money)
                    return@transaction Result.GiverDoesNotHaveSufficientFunds // get tf outta here

                // Update the sonhos of both users
                Profiles.update({ Profiles.id eq receiverProfile.id.value.toLong() }) {
                    with(SqlExpressionBuilder) {
                        it[Profiles.money] = Profiles.money + decoded.quantity
                    }
                }

                Profiles.update({ Profiles.id eq giverProfile.id.value.toLong() }) {
                    with(SqlExpressionBuilder) {
                        it[Profiles.money] = Profiles.money - decoded.quantity
                    }
                }

                // Insert transactions about it
                // Cinnamon transaction log
                val paymentResult = PaymentSonhosTransactionResults.insertAndGetId {
                    it[PaymentSonhosTransactionResults.givenBy] = giverProfile.id.value.toLong()
                    it[PaymentSonhosTransactionResults.receivedBy] = receiverProfile.id.value.toLong()
                    it[PaymentSonhosTransactionResults.sonhos] = howMuch
                    it[PaymentSonhosTransactionResults.timestamp] = now.toJavaInstant()
                }

                SimpleSonhosTransactionsLogUtils.insert(
                    receiverProfile.id.value.toLong(),
                    now.toJavaInstant(),
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
                    now.toJavaInstant(),
                    TransactionType.PAYMENT,
                    howMuch,
                    StoredPaymentSonhosTransaction(
                        giverProfile.id.value.toLong(),
                        receiverProfile.id.value.toLong(),
                        paymentResult.value
                    )
                )

                // Delete interaction ID
                loritta.pudding.interactionsData.deleteInteractionData(decodedGenericInteractionData.interactionDataId)

                // Get the profiles again
                val updatedReceiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.userId))
                val updatedGiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.sourceId))

                val receiverRanking = if (updatedReceiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(updatedReceiverProfile.money) else null
                val giverRanking = if (updatedGiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(updatedGiverProfile.money) else null

                return@transaction Result.Success(
                    decoded.userId,
                    decoded.sourceId,
                    decoded.quantity,
                    updatedReceiverProfile.money,
                    receiverRanking,
                    updatedGiverProfile.money,
                    giverRanking
                )
            }

            when (result) {
                is Result.Success -> {
                    // Let's go!!
                    transactionRequestMessage.editMessage {
                        actionRow {
                            disabledButton(
                                ButtonStyle.Primary,
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferAccepted)
                            ) {
                                loriEmoji = Emotes.LoriCard
                            }
                        }
                    }

                    context.sendMessage {
                        styled(
                            context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.SuccessfullyTransferred(mentionUser(acceptedUserId), result.howMuch)),
                            Emotes.Handshake
                        )

                        // Let's add a random emoji just to look cute
                        val user1Emote = SonhosUtils.HANGLOOSE_EMOTES.random()
                        val user2Emote = SonhosUtils.HANGLOOSE_EMOTES.filter { it != user1Emote }.random()

                        if (result.giverRanking != null) {
                            styled(
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking(mentionUser(result.giverId), SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity), result.giverQuantity, result.giverRanking)),
                                user1Emote
                            )
                        } else {
                            styled(
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos(mentionUser(result.giverId), SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity), result.giverQuantity)),
                                user1Emote
                            )
                        }
                        if (result.receiverRanking != null) {
                            styled(
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking(mentionUser(result.receiverId), SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity), result.receiverQuantity, result.receiverRanking)),
                                user2Emote
                            )
                        } else {
                            styled(
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos(mentionUser(result.receiverId), SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity), result.receiverQuantity)),
                                user2Emote
                            )
                        }
                    }
                }
                is Result.NotTheUser -> {
                    context.failEphemerally {
                        styled(
                            context.i18nContext.get(
                                I18nKeysData.Commands.YouArentTheUserSingleUser(
                                    mentionUser(result.targetId, false)
                                )
                            ),
                            Emotes.LoriRage
                        )
                    }
                }
                Result.DataIsNotPresent -> {
                    transactionRequestMessage.editMessage {
                        actionRow {
                            disabledButton(
                                ButtonStyle.Danger,
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferFailed(SonhosCommand.PAY_I18N_PREFIX.FailReasons.Expired))
                            ) {
                                loriEmoji = Emotes.LoriSob
                            }
                        }
                    }
                }
                Result.GiverDoesNotHaveSufficientFunds -> {
                    transactionRequestMessage.editMessage {
                        actionRow {
                            disabledButton(
                                ButtonStyle.Danger,
                                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferFailed(SonhosCommand.PAY_I18N_PREFIX.FailReasons.InsufficientSonhos))
                            ) {
                                loriEmoji = Emotes.LoriSob
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decodedGenericInteractionData = context.decodeDataFromComponent<StoredGenericInteractionData>()

        context.deferUpdateMessage()

        acceptSonhos(
            loritta,
            context,
            user.id,
            ComponentContextHighLevelEditableMessage(context),
            decodedGenericInteractionData
        )
    }

    private sealed class Result {
        class Success(
            val receiverId: Snowflake,
            val giverId: Snowflake,
            val howMuch: Long,
            val receiverQuantity: Long,
            val receiverRanking: Long?,
            val giverQuantity: Long,
            val giverRanking: Long?
        ) : Result()
        class NotTheUser(val targetId: Snowflake) : Result()
        object DataIsNotPresent : Result()
        object GiverDoesNotHaveSufficientFunds : Result()
    }
}