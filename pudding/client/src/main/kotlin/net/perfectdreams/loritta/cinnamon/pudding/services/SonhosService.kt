package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DropsConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.SonhosTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.Instant
import kotlin.math.ceil
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.days

class SonhosService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the rank position in the sonhos's leaderboard, based on the [sonhos] amount
     *
     * @return in what rank position the user is
     */
    // TODO: This is not a *good* way to get an user's ranking if there are duplicates, maybe use DENSE_RANK? https://www.postgresqltutorial.com/postgresql-dense_rank-function/
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long) = pudding.transaction {
        Profiles.selectAll().where { Profiles.money greaterEq sonhos and (Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis())) and (Profiles.id notInSubQuery UsersService.botTokenUsersList()) }
            .count()
    }

    suspend fun getUserTotalTransactions(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>,
        beforeDateFilter: Instant?,
        afterDateFilter: Instant?,
    ): Long {
        val userIdAsLong = userId.value.toLong()
        var query = Op.build {
            SimpleSonhosTransactionsLog.user eq userIdAsLong and (SimpleSonhosTransactionsLog.type inList transactionTypeFilter)
        }

        if (beforeDateFilter != null)
            query = query and (SimpleSonhosTransactionsLog.timestamp lessEq beforeDateFilter)

        if (afterDateFilter != null)
            query = query and (SimpleSonhosTransactionsLog.timestamp greaterEq afterDateFilter)

        return pudding.transaction {
            SimpleSonhosTransactionsLog.selectAll().where(query).count()
        }
    }

    suspend fun getUserTransactions(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>,
        limit: Int,
        offset: Long,
        beforeDateFilter: Instant?,
        afterDateFilter: Instant?,
    ): List<SonhosTransaction> {
        val userIdAsLong = userId.value.toLong()
        var query = Op.build {
            SimpleSonhosTransactionsLog.user eq userIdAsLong and (SimpleSonhosTransactionsLog.type inList transactionTypeFilter)
        }

        if (beforeDateFilter != null)
            query = query and (SimpleSonhosTransactionsLog.timestamp lessEq beforeDateFilter)

        if (afterDateFilter != null)
            query = query and (SimpleSonhosTransactionsLog.timestamp greaterEq afterDateFilter)

        return pudding.transaction {
            val rawResults = SimpleSonhosTransactionsLog.selectAll().where(query).orderBy(SimpleSonhosTransactionsLog.timestamp, SortOrder.DESC)
                .offset(offset)
                .limit(limit)
                .toList()

            val rowToStoredTransactions = rawResults.associate {
                it to Json.decodeFromString<StoredSonhosTransaction>(it[SimpleSonhosTransactionsLog.metadata])
            }

            val storedTransactions = rowToStoredTransactions.values

            // Optimization: Query all matchmaking results in a single swoop
            val globalMatchmakingResults = CoinFlipBetGlobalMatchmakingResults.selectAll()
                .where {
                    CoinFlipBetGlobalMatchmakingResults.id inList storedTransactions.filterIsInstance<StoredCoinFlipBetGlobalTransaction>().map { it.matchmakingResultId }
                }
                .toList()

            val localMatchmakingResults = CoinFlipBetMatchmakingResults.selectAll()
                .where {
                    CoinFlipBetMatchmakingResults.id inList storedTransactions.filterIsInstance<StoredCoinFlipBetTransaction>().map { it.matchmakingResultId }
                }
                .toList()

            val emojiFightMatchmakingResults = EmojiFightMatchmakingResults.selectAll()
                .where {
                    EmojiFightMatchmakingResults.id inList storedTransactions.filterIsInstance<StoredEmojiFightBetSonhosTransaction>().map { it.emojiFightMatchmakingResultsId }
                }
                .toList()

            // EmojiFightParticipants.id eq emojiFightMatchmakingResults[EmojiFightMatchmakingResults.winner]
            val emojiFightMatchmakingResultsWinnerInMatches = EmojiFightParticipants.selectAll()
                .where {
                    EmojiFightParticipants.id inList emojiFightMatchmakingResults.map { it[EmojiFightMatchmakingResults.winner] }
                }
                .toList()

            val userCountField = EmojiFightParticipants.user.count()
            val emojiFightMatchmakingResultsUsersInMatches = EmojiFightParticipants.select(EmojiFightParticipants.match, userCountField)
                .where {
                    // If it is null when we can't do anything about it
                    EmojiFightParticipants.match inList emojiFightMatchmakingResults.mapNotNull { it[EmojiFightMatchmakingResults.match] }
                }
                .groupBy(EmojiFightParticipants.match)
                .toList()

            val thirdPartyPaymentResults = ThirdPartyPaymentSonhosTransactionResults.selectAll()
                .where {
                    ThirdPartyPaymentSonhosTransactionResults.id inList storedTransactions.filterIsInstance<StoredThirdPartyPaymentSonhosTransaction>().map { it.thirdPartyPaymentId }
                }
                .toList()

            val dropsGuildIds = (storedTransactions.filterIsInstance<StoredDropChatTransaction>().map { it.guildId } + storedTransactions.filterIsInstance<StoredDropCallTransaction>().map { it.guildId }).toSet()

            val dropsConfigs = DropsConfigs.selectAll()
                .where {
                    DropsConfigs.id inList dropsGuildIds
                }
                .toList()

            rowToStoredTransactions
                .map { (it, stored) ->
                    /**
                     * Creates the [T] instance using reflection. Useful to avoid a bunch of code repetition.
                     *
                     * @param clazz the class that will be initialized
                     * @param args the args of the class
                     * @return the new [T] instance
                     */
                    fun <T : Any> createUsingReflection(
                        clazz: KClass<T>,
                        vararg args: Any?
                    ): T {
                        val kargs = args.toMutableList()
                        kargs.addAll(
                            0,
                            listOf(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                it[SimpleSonhosTransactionsLog.sonhos]
                            )
                        )

                        return clazz.primaryConstructor!!.call(*kargs.toTypedArray())
                    }

                    when (stored) {
                        is StoredShipEffectSonhosTransaction -> createUsingReflection(ShipEffectSonhosTransaction::class)

                        is StoredDailyRewardSonhosTransaction -> createUsingReflection(DailyRewardSonhosTransaction::class)

                        is StoredBotVoteSonhosTransaction -> BotVoteSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            stored.websiteSource,
                            it[SimpleSonhosTransactionsLog.sonhos]
                        )

                        is StoredDivineInterventionSonhosTransaction -> DivineInterventionSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            stored.action,
                            UserId(stored.editedBy),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.reason
                        )

                        is StoredDailyTaxSonhosTransaction -> createUsingReflection(DailyTaxSonhosTransaction::class, stored.maxDayThreshold, stored.minimumSonhosForTrigger)

                        is StoredPaymentSonhosTransaction -> {
                            PaymentSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                UserId(stored.givenBy),
                                UserId(stored.receivedBy),
                                it[SimpleSonhosTransactionsLog.sonhos],
                            )
                        }

                        is StoredAPIInitiatedPaymentSonhosTransaction -> {
                            APIInitiatedPaymentSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                UserId(stored.givenBy),
                                UserId(stored.receivedBy),
                                it[SimpleSonhosTransactionsLog.sonhos],
                                stored.reason
                            )
                        }

                        is StoredBrokerSonhosTransaction -> BrokerSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            stored.action,
                            stored.ticker,
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.stockPrice,
                            stored.stockQuantity
                        )

                        is StoredRaffleRewardTransaction -> {
                            val raffle = Raffles.selectAll().where {
                                Raffles.id eq stored.raffleId
                            }.first()

                            RaffleRewardSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                raffle[Raffles.paidOutPrize] ?: -1,
                                raffle[Raffles.paidOutPrizeAfterTax] ?: raffle[Raffles.paidOutPrize] ?: -1,
                                raffle[Raffles.tax],
                                raffle[Raffles.taxPercentage]
                            )
                        }

                        is StoredRaffleTicketsTransaction -> createUsingReflection(RaffleTicketsSonhosTransaction::class, stored.ticketQuantity)

                        is StoredSonhosBundlePurchaseTransaction -> createUsingReflection(SonhosBundlePurchaseSonhosTransaction::class)

                        is StoredCoinFlipBetTransaction -> {
                            val matchmakingResult = localMatchmakingResults.first { it[CoinFlipBetMatchmakingResults.id].value == stored.matchmakingResultId }

                            CoinFlipBetSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                UserId(matchmakingResult[CoinFlipBetMatchmakingResults.winner].value),
                                UserId(matchmakingResult[CoinFlipBetMatchmakingResults.loser].value),
                                matchmakingResult[CoinFlipBetMatchmakingResults.quantity],
                                matchmakingResult[CoinFlipBetMatchmakingResults.quantityAfterTax],
                                matchmakingResult[CoinFlipBetMatchmakingResults.tax],
                                matchmakingResult[CoinFlipBetMatchmakingResults.taxPercentage]
                            )
                        }

                        is StoredCoinFlipBetGlobalTransaction -> {
                            val matchmakingResult = globalMatchmakingResults.first { it[CoinFlipBetGlobalMatchmakingResults.id].value == stored.matchmakingResultId }

                            CoinFlipBetGlobalSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                UserId(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.winner].value),
                                UserId(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.loser].value),
                                matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantity],
                                matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax],
                                matchmakingResult[CoinFlipBetGlobalMatchmakingResults.tax],
                                matchmakingResult[CoinFlipBetGlobalMatchmakingResults.taxPercentage],
                                matchmakingResult[CoinFlipBetGlobalMatchmakingResults.timeOnQueue].toMillis()
                            )
                        }

                        is StoredThirdPartyPaymentSonhosTransaction -> {
                            val paymentResult = thirdPartyPaymentResults.first { it[ThirdPartyPaymentSonhosTransactionResults.id].value == stored.thirdPartyPaymentId }

                            ThirdPartyPaymentSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                UserId(paymentResult[ThirdPartyPaymentSonhosTransactionResults.givenBy].value),
                                UserId(paymentResult[ThirdPartyPaymentSonhosTransactionResults.receivedBy].value),
                                paymentResult[ThirdPartyPaymentSonhosTransactionResults.sonhos],
                                paymentResult[ThirdPartyPaymentSonhosTransactionResults.tax],
                                paymentResult[ThirdPartyPaymentSonhosTransactionResults.taxPercentage],
                                paymentResult[ThirdPartyPaymentSonhosTransactionResults.reason]
                            )
                        }

                        is StoredSparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            stored.action,
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.sparklyPowerSonhos,
                            stored.playerName,
                            stored.playerUniqueId,
                            stored.exchangeRate
                        )

                        is StoredChristmas2022SonhosTransaction -> createUsingReflection(Christmas2022SonhosTransaction::class, stored.gifts)

                        is StoredEaster2023SonhosTransaction -> createUsingReflection(Easter2023SonhosTransaction::class, stored.baskets)

                        is StoredReactionEventSonhosTransaction -> createUsingReflection(ReactionEventSonhosTransaction::class, stored.eventInternalId, stored.craftedCount)

                        is StoredPowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> createUsingReflection(PowerStreamClaimedFirstSonhosRewardSonhosTransaction::class, stored.liveId, stored.streamId)

                        is StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction -> createUsingReflection(PowerStreamClaimedFirstSonhosRewardSonhosTransaction::class, stored.liveId, stored.streamId)

                        is StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction -> createUsingReflection(LoriCoolCardsBoughtBoosterPackSonhosTransaction::class, stored.eventId)

                        is StoredLoriCoolCardsFinishedAlbumSonhosTransaction -> createUsingReflection(LoriCoolCardsFinishedAlbumSonhosTransaction::class, stored.eventId)

                        is StoredLoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(stored.givenBy),
                            UserId(stored.receivedBy),
                            it[SimpleSonhosTransactionsLog.sonhos],
                        )

                        is StoredLorittaItemShopBoughtBackgroundTransaction -> createUsingReflection(LorittaItemShopBoughtBackgroundTransaction::class, stored.internalBackgroundId)

                        is StoredLorittaItemShopBoughtProfileDesignTransaction -> createUsingReflection(LorittaItemShopBoughtProfileDesignTransaction::class, stored.internalProfileDesignId)

                        is StoredLorittaItemShopComissionBackgroundTransaction -> createUsingReflection(LorittaItemShopComissionBackgroundTransaction::class, stored.boughtUserId, stored.internalBackgroundId)

                        is StoredLorittaItemShopComissionProfileDesignTransaction -> createUsingReflection(LorittaItemShopComissionProfileDesignTransaction::class, stored.boughtUserId, stored.internalProfileDesignId)

                        is StoredBomDiaECiaCallCalledTransaction -> createUsingReflection(BomDiaECiaCallCalledTransaction::class)

                        is StoredBomDiaECiaCallWonTransaction -> createUsingReflection(BomDiaECiaCallWonTransaction::class)

                        is StoredGarticosTransferTransaction -> createUsingReflection(GarticosTransferTransaction::class, stored.garticos, stored.transferRate)

                        is StoredMarriageMarryTransaction -> createUsingReflection(MarriageMarryTransaction::class, stored.marriedWithUserId)

                        is StoredMarriageRestoreTransaction -> createUsingReflection(MarriageRestoreTransaction::class)

                        is StoredMarriageRestoreAutomaticTransaction -> createUsingReflection(MarriageRestoreAutomaticTransaction::class)

                        StoredMarriageLoveLetterTransaction -> createUsingReflection(MarriageLoveLetterTransaction::class)

                        is StoredChargebackedSonhosBundleTransaction -> createUsingReflection(ChargebackedSonhosBundleTransaction::class, stored.triggeredByUserId)

                        is StoredReputationDeletedTransaction -> createUsingReflection(ReputationDeletedTransaction::class, stored.reputationId)

                        is StoredBlackjackPayoutTransaction -> createUsingReflection(BlackjackPayoutTransaction::class, stored.matchId)

                        is StoredBlackjackTiedTransaction -> createUsingReflection(BlackjackTiedTransaction::class, stored.matchId)

                        is StoredBlackjackJoinedTransaction -> createUsingReflection(BlackjackJoinedTransaction::class, stored.matchId)

                        is StoredBlackjackSplitTransaction -> createUsingReflection(BlackjackSplitTransaction::class, stored.matchId)

                        is StoredBlackjackInsuranceTransaction -> createUsingReflection(BlackjackInsuranceTransaction::class, stored.matchId)

                        is StoredBlackjackInsurancePayoutTransaction -> createUsingReflection(BlackjackInsurancePayoutTransaction::class, stored.matchId)

                        is StoredBlackjackDoubleDownTransaction -> createUsingReflection(BlackjackDoubleDownTransaction::class, stored.matchId)

                        is StoredBlackjackRefundTransaction -> createUsingReflection(BlackjackRefundTransaction::class, stored.matchId)

                        is StoredMinesJoinedTransaction -> createUsingReflection(MinesJoinedTransaction::class, stored.matchId)

                        is StoredMinesPayoutTransaction -> createUsingReflection(MinesPayoutTransaction::class, stored.matchId)

                        is StoredMinesRefundTransaction -> createUsingReflection(MinesRefundTransaction::class, stored.matchId)

                        is StoredDropChatTransaction -> {
                            val dropsConfig = dropsConfigs.firstOrNull { it[DropsConfigs.id].value == stored.guildId }

                            // This is... not great
                            val plan = if (dropsConfig != null) {
                                val value = DonationKeys.selectAll().where { DonationKeys.activeIn eq dropsConfig[DropsConfigs.id] and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                                    .toList()
                                    .sumOf {
                                        // This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.38()
                                        ceil(it[DonationKeys.value])
                                    }

                                ServerPremiumPlans.getPlanFromValue(value)
                            } else {
                                ServerPremiumPlans.Free
                            }

                            val showGuildInformationOnTransactions = dropsConfig?.get(DropsConfigs.showGuildInformationOnTransactions)
                            val guildName = dropsConfig?.get(DropsConfigs.guildName)
                            val guildInviteCode = dropsConfig?.get(DropsConfigs.guildInviteCode)

                            DropChatTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                it[SimpleSonhosTransactionsLog.sonhos],
                                stored.dropId,
                                stored.charged,
                                stored.givenById,
                                stored.receivedById,
                                stored.guildId,
                                if (showGuildInformationOnTransactions == true && guildName != null && plan.showDropGuildInfoOnTransactions) {
                                    DropGuildInfo(
                                        guildName,
                                        guildInviteCode
                                    )
                                } else null
                            )
                        }

                        is StoredDropCallTransaction -> {
                            val dropsConfig = dropsConfigs.firstOrNull { it[DropsConfigs.id].value == stored.guildId }

                            // This is... not great
                            val plan = if (dropsConfig != null) {
                                val value = DonationKeys.selectAll().where { DonationKeys.activeIn eq dropsConfig[DropsConfigs.id] and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                                    .toList()
                                    .sumOf {
                                        // This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.38()
                                        ceil(it[DonationKeys.value])
                                    }

                                ServerPremiumPlans.getPlanFromValue(value)
                            } else {
                                ServerPremiumPlans.Free
                            }

                            val showGuildInformationOnTransactions = dropsConfig?.get(DropsConfigs.showGuildInformationOnTransactions)
                            val guildName = dropsConfig?.get(DropsConfigs.guildName)
                            val guildInviteCode = dropsConfig?.get(DropsConfigs.guildInviteCode)

                            DropCallTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                it[SimpleSonhosTransactionsLog.sonhos],
                                stored.dropId,
                                stored.charged,
                                stored.givenById,
                                stored.receivedById,
                                stored.guildId,
                                if (showGuildInformationOnTransactions == true && guildName != null && plan.showDropGuildInfoOnTransactions) {
                                    DropGuildInfo(
                                        guildName,
                                        guildInviteCode
                                    )
                                } else null
                            )
                        }

                        is StoredVacationModeLeaveTransaction -> createUsingReflection(VacationModeLeaveTransaction::class)
                        is StoredLotteryRewardTransaction -> createUsingReflection(LotteryRewardTransaction::class, stored.lotteryId, stored.taxed, stored.payoutWithoutTax)
                        is StoredLotteryTicketsTransaction -> createUsingReflection(LotteryTicketsTransaction::class, stored.lotteryId, stored.ticketId)

                        is StoredEmojiFightBetSonhosTransaction -> {
                            val emojiFightMatchmakingResult = emojiFightMatchmakingResults.first { it[EmojiFightMatchmakingResults.id].value == stored.emojiFightMatchmakingResultsId }

                            val winnerInMatch = emojiFightMatchmakingResultsWinnerInMatches.first { it[EmojiFightParticipants.id].value == emojiFightMatchmakingResult[EmojiFightMatchmakingResults.winner].value }

                            val usersInMatch = emojiFightMatchmakingResultsUsersInMatches.first { it[EmojiFightParticipants.match] == winnerInMatch[EmojiFightParticipants.match] }[userCountField]

                            EmojiFightBetSonhosTransaction(
                                it[SimpleSonhosTransactionsLog.id].value,
                                it[SimpleSonhosTransactionsLog.type],
                                it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                                UserId(it[SimpleSonhosTransactionsLog.user].value),
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.id].value,
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.match]?.value,
                                UserId(winnerInMatch[EmojiFightParticipants.user].value),
                                usersInMatch,
                                winnerInMatch[EmojiFightParticipants.emoji],
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.entryPrice],
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.entryPriceAfterTax],
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.tax],
                                emojiFightMatchmakingResult[EmojiFightMatchmakingResults.taxPercentage]
                            )
                        }
                    }
                }
        }
    }

    /**
     * Gets the user's last received daily reward
     *
     * @param userId    the user's ID
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(userId: UserId, afterTime: kotlinx.datetime.Instant): Daily? {
        return pudding.transaction {
            val timeInMillis = afterTime.toEpochMilliseconds()

            val dailyResult = Dailies.selectAll().where {
                Dailies.receivedById eq userId.value.toLong() and (Dailies.receivedAt greaterEq timeInMillis)
            }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()

            return@transaction if (dailyResult != null)
                Daily.fromRow(dailyResult)
            else null
        }
    }

    suspend fun userGotDailyRecently(userId: Long, numberOfDays: Int): Boolean {
        return getUserLastDailyRewardReceived(
            UserId(userId),
            Clock.System.now() - numberOfDays.days
        ) != null
    }
}