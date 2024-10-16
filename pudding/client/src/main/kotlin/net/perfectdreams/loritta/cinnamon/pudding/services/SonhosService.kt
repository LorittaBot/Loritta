package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.SonhosTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.Instant
import kotlin.time.Duration.Companion.days

class SonhosService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the rank position in the sonhos's leaderboard, based on the [sonhos] amount
     *
     * @return in what rank position the user is
     */
    // TODO: This is not a *good* way to get an user's ranking if there are duplicates, maybe use DENSE_RANK? https://www.postgresqltutorial.com/postgresql-dense_rank-function/
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long) = pudding.transaction {
        Profiles.select { Profiles.money greaterEq sonhos and (Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis())) }
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
            SimpleSonhosTransactionsLog.select(query).count()
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
            val rawResults = SimpleSonhosTransactionsLog.select(query).orderBy(SimpleSonhosTransactionsLog.timestamp, SortOrder.DESC)
                .limit(limit, offset)
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

            rowToStoredTransactions
                .map { (it, stored) ->
                    when (stored) {
                        is StoredShipEffectSonhosTransaction -> ShipEffectSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos]
                        )

                        is StoredDailyRewardSonhosTransaction -> DailyRewardSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos]
                        )

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

                        is StoredDailyTaxSonhosTransaction -> DailyTaxSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.maxDayThreshold,
                            stored.minimumSonhosForTrigger
                        )

                        is StoredPaymentSonhosTransaction -> PaymentSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(stored.givenBy),
                            UserId(stored.receivedBy),
                            it[SimpleSonhosTransactionsLog.sonhos],
                        )

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
                            val raffle = Raffles.select {
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

                        is StoredRaffleTicketsTransaction -> RaffleTicketsSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.ticketQuantity
                        )

                        is StoredSonhosBundlePurchaseTransaction -> SonhosBundlePurchaseSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos]
                        )

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

                        is StoredChristmas2022SonhosTransaction -> Christmas2022SonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.gifts
                        )

                        is StoredEaster2023SonhosTransaction -> Easter2023SonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.baskets
                        )

                        is StoredPowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.liveId,
                            stored.streamId
                        )

                        is StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.liveId,
                            stored.streamId
                        )

                        is StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.eventId
                        )

                        is StoredLoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.eventId
                        )

                        is StoredLoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(stored.givenBy),
                            UserId(stored.receivedBy),
                            it[SimpleSonhosTransactionsLog.sonhos],
                        )

                        is StoredLorittaItemShopBoughtBackgroundTransaction -> LorittaItemShopBoughtBackgroundTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.internalBackgroundId
                        )

                        is StoredLorittaItemShopBoughtProfileDesignTransaction -> LorittaItemShopBoughtProfileDesignTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            it[SimpleSonhosTransactionsLog.sonhos],
                            stored.internalProfileDesignId
                        )

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

            val dailyResult = Dailies.select {
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