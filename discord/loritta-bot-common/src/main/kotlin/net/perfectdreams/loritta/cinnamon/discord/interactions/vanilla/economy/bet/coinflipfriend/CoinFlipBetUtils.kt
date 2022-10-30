package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipfriend

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.toMessageFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.interactions.HighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.SlashContextHighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.disabledButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithDatabaseData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.editMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class CoinFlipBetUtils(val loritta: LorittaBot) {
    suspend fun createBet(
        context: InteractionContext,
        howMuch: Long,
        receiverId: Snowflake,
        combo: Int
    ) {
        val isLoritta = receiverId == loritta.config.loritta.discord.applicationId
        val ttlDuration = 3.minutes

        checkIfSelfAccountIsOldEnough(context)
        checkIfOtherAccountIsOldEnough(context, receiverId)
        // TODO: Reenable this
        // checkIfSelfAccountGotDailyRecently(context)

        if (UserUtils.handleIfUserIsBanned(loritta, context, receiverId))
            return

        // Only defer if it wasn't deferred yet
        if (!context.interaKTionsContext.isDeferred)
            context.deferChannelMessage()

        val userProfile = loritta.pudding.users.getUserProfile(UserId(context.user.id))
        if (userProfile == null || howMuch > userProfile.money) {
            context.fail {
                styled(
                    context.i18nContext.get(SonhosUtils.insufficientSonhos(userProfile, howMuch)),
                    Emotes.LoriSob
                )

                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                    loritta,
                    context.i18nContext,
                    UserId(context.user.id),
                    "bet-coinflip-friend",
                    "mm-check"
                )
            }
        }

        val receiverProfile = loritta.pudding.users.getUserProfile(UserId(receiverId))
        if (receiverProfile == null || howMuch > receiverProfile.money) {
            // Receiver does not have enough sonhos
            context.fail {
                styled(
                    context.i18nContext.get(
                        BetCommand.COINFLIP_FRIEND_I18N_PREFIX.InsufficientFundsInvited(
                            mentionUser(
                                receiverId,
                                notifyUser = false
                            ), howMuch, howMuch - (receiverProfile?.money ?: 0L)
                        )
                    ),
                    Emotes.LoriSob
                )
            }
        }

        val selfActiveDonations = loritta.pudding.payments.getActiveMoneyFromDonations(UserId(context.user.id))
        val otherActiveDonations = loritta.pudding.payments.getActiveMoneyFromDonations(UserId(receiverId))

        val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
        val otherPlan = UserPremiumPlans.getPlanFromValue(otherActiveDonations)

        val hasNoTax: Boolean
        val whoHasTheNoTaxReward: Snowflake?
        val plan: UserPremiumPlans?
        val tax: Long?
        val taxPercentage: Double?
        val quantityAfterTax: Long
        val money: Long

        if (selfPlan.totalCoinFlipReward == 1.0) {
            whoHasTheNoTaxReward = context.user.id
            hasNoTax = true
            plan = selfPlan
            taxPercentage = null
            tax = null
            money = howMuch
        } else if (otherPlan.totalCoinFlipReward == 1.0) {
            whoHasTheNoTaxReward = receiverId
            hasNoTax = true
            plan = otherPlan
            taxPercentage = null
            tax = null
            money = howMuch
        } else {
            whoHasTheNoTaxReward = null
            hasNoTax = false
            plan = UserPremiumPlans.Essential
            taxPercentage =
                (1.0.toBigDecimal() - selfPlan.totalCoinFlipReward.toBigDecimal()).toDouble() // Avoid rounding errors
            tax = (howMuch * taxPercentage).toLong()
            money = howMuch - tax
        }

        if (!hasNoTax && tax == 0L)
            context.fail {
                styled(
                    context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.YouNeedToBetMore),
                    Emotes.Error
                )
            }

        // We WANT to store on the database due to two things:
        // 1. We want to control the interaction TTL
        // 2. We want to block duplicate transactions by buttom spamming (with this, we can block this on transaction level)
        val nowPlusTimeToLive = Clock.System.now() + ttlDuration

        val (interactionDataId, data, encodedData) = context.loritta.encodeDataForComponentOnDatabase(
            AcceptCoinFlipBetFriendData(
                receiverId,
                context.user.id,
                howMuch,
                money,
                tax,
                taxPercentage,
                combo
            ),
            ttl = ttlDuration
        )

        val message = context.sendMessage {
            if (hasNoTax) {
                styled(
                    context.i18nContext.get(
                        BetCommand.COINFLIP_FRIEND_I18N_PREFIX.StartBetNoTax(
                            friendMention = mentionUser(receiverId),
                            userMention = mentionUser(context.user),
                            betSonhosQuantity = money,
                            userWithNoTaxMention = mentionUser(whoHasTheNoTaxReward!!),
                        )
                    ),
                    Emotes.LoriRich
                )
            } else {
                styled(
                    context.i18nContext.get(
                        BetCommand.COINFLIP_FRIEND_I18N_PREFIX.StartBet(
                            friendMention = mentionUser(receiverId),
                            userMention = mentionUser(context.user),
                            betSonhosQuantity = howMuch,
                            taxSonhosQuantity = tax!!,
                            betSonhosQuantityAfterTax = money
                        )
                    ),
                    Emotes.LoriRich
                )
            }

            styled(
                context.i18nContext.get(
                    SonhosCommand.PAY_I18N_PREFIX.ConfirmTheTransaction(
                        mentionUser(receiverId), nowPlusTimeToLive.toMessageFormat(
                            DiscordTimestampStyle.LongDateTime
                        ), nowPlusTimeToLive.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                    )
                ),
                Emotes.LoriZap
            )

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.AcceptBet),
                    AcceptCoinFlipBetFriendButtonExecutor,
                    encodedData
                ) {
                    loriEmoji = Emotes.Handshake
                }

                /* interactiveButton(
                    ButtonStyle.Danger,
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.Cancel),
                    CancelSonhosTransferButtonExecutor,
                    context.loritta.encodeDataForComponentOrStoreInDatabase(
                        CancelSonhosTransferData(
                            context.user.id,
                            interactionDataId
                        )
                    )
                ) {
                    loriEmoji = Emotes.LoriHmpf
                } */
            }
        }

        if (isLoritta) {
            // If it is Loritta, we will mimick that she is *actually* accepting the bet!
            acceptBet(
                context,
                loritta.config.loritta.discord.applicationId,
                SlashContextHighLevelEditableMessage(message),
                data
            )
        }
    }

    suspend fun acceptBet(
        context: InteractionContext,
        acceptedUserId: Snowflake,
        betRequestMessage: HighLevelEditableMessage,
        decodedGenericInteractionData: StoredGenericInteractionData
    ) {
        val result = loritta.pudding.transaction {
            val dataFromDatabase =
                loritta.pudding.interactionsData.getInteractionData(decodedGenericInteractionData.interactionDataId)
                    ?: return@transaction Result.DataIsNotPresent // Data is not present! Maybe it expired or it was already processed

            val decoded = Json.decodeFromJsonElement<AcceptCoinFlipBetFriendData>(dataFromDatabase)
            if (decoded.userId != acceptedUserId)
                return@transaction Result.NotTheUser(decoded.userId)

            val now = Clock.System.now()
            val jtNow = now.toJavaInstant()

            val receiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.userId))
            val giverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.sourceId))

            if (decoded.quantity > giverProfile.money)
                return@transaction Result.GiverDoesNotHaveSufficientFunds // get tf outta here

            if (decoded.quantity > receiverProfile.money)
                return@transaction Result.ReceiverDoesNotHaveSufficientFunds // get tf outta here²

            val isTails = loritta.random.nextBoolean()

            val winnerId: Snowflake
            val loserId: Snowflake

            if (isTails) {
                winnerId = decoded.sourceId
                loserId = decoded.userId
            } else {
                winnerId = decoded.userId
                loserId = decoded.sourceId
            }

            // Update the sonhos of both users
            Profiles.update({ Profiles.id eq winnerId.value.toLong() }) {
                with(SqlExpressionBuilder) {
                    it[Profiles.money] = Profiles.money + decoded.quantityAfterTax
                }
            }

            Profiles.update({ Profiles.id eq loserId.value.toLong() }) {
                with(SqlExpressionBuilder) {
                    it[Profiles.money] = Profiles.money - decoded.quantityAfterTax
                }
            }

            // Insert transactions about it
            val mmResult = CoinFlipBetMatchmakingResults.insertAndGetId {
                it[CoinFlipBetMatchmakingResults.timestamp] = jtNow
                it[CoinFlipBetMatchmakingResults.winner] = winnerId.toLong()
                it[CoinFlipBetMatchmakingResults.loser] = loserId.toLong()
                it[CoinFlipBetMatchmakingResults.quantity] = decoded.quantity
                it[CoinFlipBetMatchmakingResults.quantityAfterTax] = decoded.quantityAfterTax
                it[CoinFlipBetMatchmakingResults.tax] = decoded.tax
                it[CoinFlipBetMatchmakingResults.taxPercentage] = decoded.taxPercentage
            }

            val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                it[SonhosTransactionsLog.user] = winnerId.toLong()
                it[SonhosTransactionsLog.timestamp] = jtNow
            }

            CoinFlipBetSonhosTransactionsLog.insert {
                it[CoinFlipBetSonhosTransactionsLog.timestampLog] = winnerTransactionLogId
                it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
            }

            val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                it[SonhosTransactionsLog.user] = loserId.toLong()
                it[SonhosTransactionsLog.timestamp] = jtNow
            }

            CoinFlipBetSonhosTransactionsLog.insert {
                it[CoinFlipBetSonhosTransactionsLog.timestampLog] = loserTransactionLogId
                it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
            }

            // Delete interaction ID
            loritta.pudding.interactionsData.deleteInteractionData(decodedGenericInteractionData.interactionDataId)

            // Get the profiles again
            val updatedReceiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.userId))
            val updatedGiverProfile = loritta.pudding.users.getOrCreateUserProfile(UserId(decoded.sourceId))

            val receiverRanking =
                if (updatedReceiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(
                    updatedReceiverProfile.money
                ) else null
            val giverRanking =
                if (updatedGiverProfile.money != 0L) loritta.pudding.sonhos.getSonhosRankPositionBySonhos(
                    updatedGiverProfile.money
                ) else null

            return@transaction Result.Success(
                winnerId,
                loserId,
                isTails,
                decoded.quantity,
                decoded.quantityAfterTax,
                decoded.tax,
                decoded.taxPercentage,
                updatedReceiverProfile.money,
                receiverRanking,
                updatedGiverProfile.money,
                giverRanking,
                decoded.combo
            )
        }

        when (result) {
            is Result.Success -> {
                val emote = if (result.isTails) {
                    Emotes.CoinTails
                } else {
                    Emotes.CoinHeads
                }

                // Let's go!!
                betRequestMessage.editMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Primary,
                            context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.BetAccepted)
                        ) {
                            loriEmoji = emote
                        }
                    }
                }

                context.sendMessage {
                    if (result.combo != 0) {
                        styled(
                            "**Combo! X${result.combo}**"
                        )
                    }

                    if (result.isTails) {
                        styled(
                            "**${context.i18nContext.get(CoinFlipCommand.I18N_PREFIX.Tails)}**!",
                            emote
                        )
                    } else {
                        styled(
                            "**${context.i18nContext.get(CoinFlipCommand.I18N_PREFIX.Heads)}**!",
                            emote
                        )
                    }

                    styled(
                        context.i18nContext.get(
                            BetCommand.COINFLIP_FRIEND_I18N_PREFIX.Congratulations(
                                winnerMention = mentionUser(result.receiverId),
                                quantityEmoji = SonhosUtils.getSonhosEmojiOfQuantity(result.quantityAfterTax),
                                sonhos = result.quantityAfterTax,
                                loserMention = mentionUser(result.giverId)
                            )
                        ),
                        Emotes.Handshake
                    )

                    if (result.giverRanking != null) {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking(
                                    mentionUser(result.giverId),
                                    SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity),
                                    result.giverQuantity,
                                    result.giverRanking
                                )
                            ),
                            Emotes.LoriSunglasses
                        )
                    } else {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos(
                                    mentionUser(result.giverId),
                                    SonhosUtils.getSonhosEmojiOfQuantity(result.giverQuantity),
                                    result.giverQuantity
                                )
                            ),
                            Emotes.LoriSunglasses
                        )
                    }
                    if (result.receiverRanking != null) {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.TransferredSonhosWithRanking(
                                    mentionUser(result.receiverId),
                                    SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity),
                                    result.receiverQuantity,
                                    result.receiverRanking
                                )
                            ),
                            Emotes.LoriBonk
                        )
                    } else {
                        styled(
                            context.i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.TransferredSonhos(
                                    mentionUser(result.receiverId),
                                    SonhosUtils.getSonhosEmojiOfQuantity(result.receiverQuantity),
                                    result.receiverQuantity
                                )
                            ),
                            Emotes.LoriBonk
                        )
                    }

                    actionRow {
                        interactiveButtonWithDatabaseData(
                            loritta,
                            ButtonStyle.Primary,
                            RematchCoinFlipBetFriendButtonExecutor,
                            RematchCoinFlipBetFriendData(
                                context.user.id,
                                if (context.user.id == result.receiverId) result.giverId else result.receiverId,
                                result.quantity,
                                result.combo
                            )
                        ) {
                            label = "Revanche"
                        }
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
                betRequestMessage.editMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Danger,
                            context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.BetFailed(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.FailReasons.Expired))
                        ) {
                            loriEmoji = Emotes.LoriSob
                        }
                    }
                }
            }

            Result.GiverDoesNotHaveSufficientFunds, Result.ReceiverDoesNotHaveSufficientFunds -> {
                betRequestMessage.editMessage {
                    actionRow {
                        disabledButton(
                            ButtonStyle.Danger,
                            context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.BetFailed(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.FailReasons.InsufficientSonhos))
                        ) {
                            loriEmoji = Emotes.LoriSob
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkIfSelfAccountGotDailyRecently(context: InteractionContext) {
        val now = Clock.System.now()

        // Check if the user got daily in the last 14 days before allowing a transaction
        val gotDailyRewardInTheLastXDays = context.loritta.pudding.sonhos.getUserLastDailyRewardReceived(
            UserId(context.user.id),
            now - 14.days
        ) != null

        if (!gotDailyRewardInTheLastXDays)
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.SelfAccountNeedsToGetDaily(context.loritta.commandMentions.daily)),
                Emotes.LoriSob
            )
    }

    private fun checkIfSelfAccountIsOldEnough(context: InteractionContext) {
        val now = Clock.System.now()
        val timestamp = context.user.id.timestamp
        val allowedAfterTimestamp = timestamp + (14.days)

        if (allowedAfterTimestamp > now) // 14 dias
            context.failEphemerally(
                context.i18nContext.get(
                    SonhosCommand.PAY_I18N_PREFIX.SelfAccountIsTooNew(
                        allowedAfterTimestamp.toMessageFormat(
                            DiscordTimestampStyle.LongDateTime
                        ), allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                    )
                ),
                Emotes.LoriSob
            )
    }

    private fun checkIfOtherAccountIsOldEnough(context: InteractionContext, targetId: Snowflake) {
        val now = Clock.System.now()
        val timestamp = targetId.timestamp
        val allowedAfterTimestamp = timestamp + (7.days)

        if (timestamp + (7.days) > now) // 7 dias
            context.failEphemerally {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.PAY_I18N_PREFIX.OtherAccountIsTooNew(
                            mentionUser(targetId),
                            allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.LongDateTime),
                            allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                        )
                    ),
                    Emotes.LoriSob
                )
            }
    }

    private sealed class Result {
        class Success(
            val receiverId: Snowflake,
            val giverId: Snowflake,
            val isTails: Boolean,
            val quantity: Long,
            val quantityAfterTax: Long,
            val tax: Long?,
            val taxPercentage: Double?,
            val receiverQuantity: Long,
            val receiverRanking: Long?,
            val giverQuantity: Long,
            val giverRanking: Long?,
            val combo: Int
        ) : Result()

        class NotTheUser(val targetId: Snowflake) : Result()
        object DataIsNotPresent : Result()
        object GiverDoesNotHaveSufficientFunds : Result()
        object ReceiverDoesNotHaveSufficientFunds : Result()
    }
}