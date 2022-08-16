package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.PayCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.StoredGenericInteractionData
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PaymentSonhosTransactionsLog
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

class TransferSonhosButtonExecutor(
    loritta: LorittaCinnamon
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.TRANSFER_SONHOS_BUTTON_EXECUTOR) {
        private val HANGLOOSE_EMOTES = listOf(
            Emotes.LoriHanglooseRight,
            Emotes.GabrielaHanglooseRight,
            Emotes.PantufaHanglooseRight,
            Emotes.PowerHanglooseRight
        )
    }

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()

        val decodedGenericInteractionData = context.decodeDataFromComponent<StoredGenericInteractionData>()

        val result = loritta.services.transaction {
            val dataFromDatabase = loritta.services.interactionsData.getInteractionData(decodedGenericInteractionData.interactionDataId) ?: return@transaction Result.DataIsNotPresent // Data is not present! Maybe it expired or it was already processed

            val decoded = Json.decodeFromJsonElement<TransferSonhosData>(dataFromDatabase)
            if (decoded.userId != user.id)
                return@transaction Result.NotTheUser(decoded.userId)

            val howMuch = decoded.quantity
            val now = Clock.System.now()

            val receiverProfile = loritta.services.users.getOrCreateUserProfile(UserId(decoded.userId))
            val giverProfile = loritta.services.users.getOrCreateUserProfile(UserId(decoded.sourceId))

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
            val paymentResult = PaymentSonhosTransactionResults.insertAndGetId {
                it[PaymentSonhosTransactionResults.givenBy] = giverProfile.id.value.toLong()
                it[PaymentSonhosTransactionResults.receivedBy] = receiverProfile.id.value.toLong()
                it[PaymentSonhosTransactionResults.sonhos] = howMuch
                it[PaymentSonhosTransactionResults.timestamp] = now.toJavaInstant()
            }

            val giverTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                it[SonhosTransactionsLog.user] = giverProfile.id.value.toLong()
                it[SonhosTransactionsLog.timestamp] = now.toJavaInstant()
            }

            PaymentSonhosTransactionsLog.insert {
                it[PaymentSonhosTransactionsLog.timestampLog] = giverTransactionLogId
                it[PaymentSonhosTransactionsLog.paymentResult] = paymentResult
            }

            val receiverTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                it[SonhosTransactionsLog.user] = receiverProfile.id.value.toLong()
                it[SonhosTransactionsLog.timestamp] = now.toJavaInstant()
            }

            PaymentSonhosTransactionsLog.insert {
                it[PaymentSonhosTransactionsLog.timestampLog] = receiverTransactionLogId
                it[PaymentSonhosTransactionsLog.paymentResult] = paymentResult
            }

            // Delete interaction ID
            loritta.services.interactionsData.deleteInteractionData(decodedGenericInteractionData.interactionDataId)

            // Get the profiles again
            val updatedReceiverProfile = loritta.services.users.getOrCreateUserProfile(UserId(decoded.userId))
            val updatedGiverProfile = loritta.services.users.getOrCreateUserProfile(UserId(decoded.sourceId))

            val receiverRanking = if (updatedReceiverProfile.money != 0L) loritta.services.sonhos.getSonhosRankPositionBySonhos(updatedReceiverProfile.money) else null
            val giverRanking = if (updatedGiverProfile.money != 0L) loritta.services.sonhos.getSonhosRankPositionBySonhos(updatedGiverProfile.money) else null

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
                context.updateMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Primary,
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferAccepted)
                        ) {
                            loriEmoji = Emotes.LoriCard
                        }
                    }
                }

                context.sendMessage {
                    styled(
                        context.i18nContext.get(PayCommand.I18N_PREFIX.SuccessfullyTransferred(mentionUser(user), result.howMuch)),
                        Emotes.Handshake
                    )

                    // Let's add a random emoji just to look cute
                    val user1Emote = HANGLOOSE_EMOTES.random()
                    val user2Emote = HANGLOOSE_EMOTES.filter { it != user1Emote }.random()

                    if (result.giverRanking != null) {
                        styled(
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferredSonhosWithRanking(mentionUser(result.giverId), result.giverQuantity, result.giverRanking)),
                            user1Emote
                        )
                    } else {
                        styled(
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferredSonhos(mentionUser(result.giverId), result.giverQuantity)),
                            user1Emote
                        )
                    }
                    if (result.receiverRanking != null) {
                        styled(
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferredSonhosWithRanking(mentionUser(result.receiverId), result.receiverQuantity, result.receiverRanking)),
                            user2Emote
                        )
                    } else {
                        styled(
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferredSonhos(mentionUser(result.receiverId), result.giverQuantity)),
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
                context.updateMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Danger,
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferFailed(PayCommand.I18N_PREFIX.FailReasons.Expired))
                        ) {
                            loriEmoji = Emotes.LoriSob
                        }
                    }
                }
            }
            Result.GiverDoesNotHaveSufficientFunds -> {
                context.updateMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Danger,
                            context.i18nContext.get(PayCommand.I18N_PREFIX.TransferFailed(PayCommand.I18N_PREFIX.FailReasons.InsufficientSonhos))
                        ) {
                            loriEmoji = Emotes.LoriSob
                        }
                    }
                }
            }
        }
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