package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingQueue
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.hours

class BetsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getCoinFlipBetGlobalMatchmakingStats(
        quantities: List<Long>,
        cutoff: kotlinx.datetime.Instant = kotlinx.datetime.Instant.DISTANT_PAST
    ): Map<Long, CoinFlipBetGlobalMatchmakingQuantityStats> {
        return pudding.transaction {
            _cleanUpMatchmakingQueue()

            val javaCutoff = cutoff.toJavaInstant()

            val avgTimeOnQueueField = CoinFlipBetGlobalMatchmakingResults.timeOnQueue.avg()
            val quantityCount = CoinFlipBetGlobalMatchmakingResults.quantity.count()

            val averageTimeOnQueueData = CoinFlipBetGlobalMatchmakingResults.slice(CoinFlipBetGlobalMatchmakingResults.quantity, avgTimeOnQueueField)
                .select {
                    CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
                }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val recentMatchesData = CoinFlipBetGlobalMatchmakingResults.slice(CoinFlipBetGlobalMatchmakingResults.quantity, quantityCount).select {
                CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val playerPresentInMatchmakingQueueData = CoinFlipBetGlobalMatchmakingQueue.slice(CoinFlipBetGlobalMatchmakingQueue.quantity).select {
                CoinFlipBetGlobalMatchmakingQueue.quantity inList quantities
            }.groupBy(CoinFlipBetGlobalMatchmakingQueue.quantity)

            return@transaction quantities.associateWith {
                CoinFlipBetGlobalMatchmakingQuantityStats(
                    averageTimeOnQueueData.firstOrNull { row -> row[CoinFlipBetGlobalMatchmakingResults.quantity] == it }
                        ?.getOrNull(avgTimeOnQueueField)?.toLong()?.let {
                            Duration.ofNanos(it)
                        },
                    recentMatchesData.firstOrNull { row ->
                        row[CoinFlipBetGlobalMatchmakingResults.quantity] == it
                    }?.getOrNull(quantityCount) ?: 0L,
                    playerPresentInMatchmakingQueueData.any { row -> row[CoinFlipBetGlobalMatchmakingQueue.quantity] == it }
                )
            }
        }
    }

    suspend fun getUserCoinFlipBetGlobalMatchmakingStats(
        userId: UserId,
        quantities: List<Long>,
        cutoff: kotlinx.datetime.Instant = kotlinx.datetime.Instant.DISTANT_PAST
    ): Map<Long, UserSpecificCoinFlipBetGlobalMatchmakingQuantityStats> {
        return pudding.transaction {
            _cleanUpMatchmakingQueue()

            val javaCutoff = cutoff.toJavaInstant()

            val avgTimeOnQueueField = CoinFlipBetGlobalMatchmakingResults.timeOnQueue.avg()
            val quantityCount = CoinFlipBetGlobalMatchmakingResults.quantity.count()

            val averageTimeOnQueueData = CoinFlipBetGlobalMatchmakingResults.slice(CoinFlipBetGlobalMatchmakingResults.quantity, avgTimeOnQueueField)
                .select {
                    CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
                }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val recentMatchesData = CoinFlipBetGlobalMatchmakingResults.slice(CoinFlipBetGlobalMatchmakingResults.quantity, quantityCount).select {
                CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val playerPresentInMatchmakingQueue = CoinFlipBetGlobalMatchmakingQueue.slice(CoinFlipBetGlobalMatchmakingQueue.quantity).select {
                CoinFlipBetGlobalMatchmakingQueue.quantity inList quantities
            }.groupBy(CoinFlipBetGlobalMatchmakingQueue.quantity)

            val currentPlayerPresentInMatchmakingQueue = CoinFlipBetGlobalMatchmakingQueue.slice(CoinFlipBetGlobalMatchmakingQueue.quantity).select {
                CoinFlipBetGlobalMatchmakingQueue.quantity inList quantities and (CoinFlipBetGlobalMatchmakingQueue.user eq userId.value.toLong())
            }.groupBy(CoinFlipBetGlobalMatchmakingQueue.quantity)

            return@transaction quantities.associateWith {
                UserSpecificCoinFlipBetGlobalMatchmakingQuantityStats(
                    averageTimeOnQueueData.firstOrNull { row -> row[CoinFlipBetGlobalMatchmakingResults.quantity] == it }
                        ?.getOrNull(avgTimeOnQueueField)?.toLong()?.let {
                            Duration.ofNanos(it)
                        },
                    recentMatchesData.firstOrNull { row ->
                        row[CoinFlipBetGlobalMatchmakingResults.quantity] == it
                    }?.getOrNull(quantityCount) ?: 0L,
                    playerPresentInMatchmakingQueue.any { row -> row[CoinFlipBetGlobalMatchmakingQueue.quantity] == it },
                    currentPlayerPresentInMatchmakingQueue.any { row -> row[CoinFlipBetGlobalMatchmakingQueue.quantity] == it }
                )
            }
        }
    }

    suspend fun getCoinFlipBetGlobalUserBetsStats(
        userId: UserId,
        cutoff: kotlinx.datetime.Instant = kotlinx.datetime.Instant.DISTANT_PAST
    ): UserCoinFlipBetGlobalStats {
        val userAsLong = userId.value.toLong()

        return pudding.transaction {
            _cleanUpMatchmakingQueue()

            val sumField = CoinFlipBetGlobalMatchmakingResults.quantity.sum()
            val javaCutoff = cutoff.toJavaInstant()

            val winCount = CoinFlipBetGlobalMatchmakingResults.slice(sumField).select {
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.count()

            val lostCount = CoinFlipBetGlobalMatchmakingResults.slice(sumField).select {
                (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.count()

            val winSum = CoinFlipBetGlobalMatchmakingResults.slice(sumField).select {
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.firstOrNull()?.getOrNull(sumField) ?: 0L

            val lostSum = CoinFlipBetGlobalMatchmakingResults.slice(sumField).select {
                (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.firstOrNull()?.getOrNull(sumField) ?: 0L

            return@transaction UserCoinFlipBetGlobalStats(
                winCount,
                lostCount,
                winSum,
                lostSum
            )
        }
    }

    suspend fun removeFromCoinFlipBetGlobalMatchmakingQueue(
        userId: UserId,
        quantity: Long
    ): Boolean {
        return pudding.transaction {
            _cleanUpMatchmakingQueue()

            CoinFlipBetGlobalMatchmakingQueue.deleteWhere {
                CoinFlipBetGlobalMatchmakingQueue.user eq userId.value.toLong() and (CoinFlipBetGlobalMatchmakingQueue.quantity eq quantity)
            } != 0
        }
    }

    suspend fun addToCoinFlipBetGlobalMatchmakingQueue(
        userId: UserId,
        userInteractionToken: String,
        quantity: Long
    ): List<CoinFlipGlobalMatchmakingResult> {
        return pudding.transaction {
            val now = Instant.now()
            _cleanUpMatchmakingQueue()

            // Used for matchmaking results, because some responses may have multiple results
            // (Example: if someone joins the matchmaking queue but doesn't have enough sonhos for it)
            val results = mutableListOf<CoinFlipGlobalMatchmakingResult>()

            // Get if the user is already on the matchmaking queue for the current quantity
            val selfUserMatchmakingQueueCount = CoinFlipBetGlobalMatchmakingQueue.select {
                CoinFlipBetGlobalMatchmakingQueue.user eq userId.value.toLong() and (CoinFlipBetGlobalMatchmakingQueue.quantity eq quantity)
            }.count()

            if (selfUserMatchmakingQueueCount != 0L)
                return@transaction results.apply { add(AlreadyInQueueResult()) }

            // If not, we are going to check if there is anyone else on the matchmaking queue that isn't ourselves...
            val anotherUserMatchmakingData = CoinFlipBetGlobalMatchmakingQueue.select {
                CoinFlipBetGlobalMatchmakingQueue.user neq userId.value.toLong() and (CoinFlipBetGlobalMatchmakingQueue.quantity eq quantity)
            }.firstOrNull()

            // Create self profile
            val profile = pudding.users._getOrCreateUserProfile(userId)

            if (quantity > profile.money)
                return@transaction results.apply { add(YouDontHaveEnoughSonhosToBetResult()) }

            if (anotherUserMatchmakingData != null) {
                // Check if both users have enough sonhos
                val anotherUserProfile = pudding.users._getOrCreateUserProfile(
                    UserId(
                        anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.user].value
                    )
                )

                if (quantity > anotherUserProfile.money) {
                    // The other user doesn't have enough sonhos to participate, let's remove them from the global matchmaking queue and then we will notify that they were removed
                    CoinFlipBetGlobalMatchmakingQueue.deleteWhere {
                        CoinFlipBetGlobalMatchmakingQueue.id eq anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.id]
                    }

                    results.add(
                        AnotherUserRemovedFromMatchmakingQueueResult(
                            UserId(anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.user].value),
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken]
                        )
                    )
                } else {
                    val otherUserId = UserId(anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.user].value)

                    // Do matchmaking stuff
                    CoinFlipBetGlobalMatchmakingQueue.deleteWhere {
                        CoinFlipBetGlobalMatchmakingQueue.id eq anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.id]
                    }

                    val selfActiveDonations = pudding.payments._getActiveMoneyFromDonations(userId)
                    val otherUserActiveDonations = pudding.payments._getActiveMoneyFromDonations(otherUserId)

                    // TODO: Don't hardcode this! Move this to somewhere else
                    val taxPercentage = 0.05
                    // "Recommended" plan (R$ 40) has non-tax for coinflip
                    // We check >= 25 because... idk, it doesn't really matter
                    val noSonhosTax = selfActiveDonations >= 25 || otherUserActiveDonations >= 25

                    val premiumUsers = mutableListOf<UserId>()
                    if (selfActiveDonations >= 25)
                        premiumUsers.add(userId)
                    if (otherUserActiveDonations >= 25)
                        premiumUsers.add(otherUserId)

                    val tax: Long?
                    val quantityAfterTax: Long

                    if (noSonhosTax) {
                        tax = null
                        quantityAfterTax = quantity
                    } else {
                        tax = (quantity * taxPercentage).toLong()
                        quantityAfterTax = quantity - tax
                    }

                    val isTails = pudding.random.nextBoolean()

                    val players = mutableListOf(
                        userId,
                        otherUserId
                    )

                    val winner: UserId
                    val loser: UserId

                    if (isTails) {
                        winner = players[0]
                        loser = players[1]
                    } else {
                        winner = players[1]
                        loser = players[0]
                    }

                    val resultId = CoinFlipBetGlobalMatchmakingResults.insertAndGetId {
                        it[CoinFlipBetGlobalMatchmakingResults.winner] = winner.value.toLong()
                        it[CoinFlipBetGlobalMatchmakingResults.loser] = loser.value.toLong()
                        it[CoinFlipBetGlobalMatchmakingResults.quantity] = quantity
                        it[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax] = quantityAfterTax
                        it[CoinFlipBetGlobalMatchmakingResults.tax] = tax
                        it[CoinFlipBetGlobalMatchmakingResults.taxPercentage] = taxPercentage
                        it[CoinFlipBetGlobalMatchmakingResults.timestamp] = now
                        it[CoinFlipBetGlobalMatchmakingResults.timeOnQueue] = Duration.between(anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.timestamp], now)
                    }

                    if (quantity != 0L) {
                        // If the quantity is not zero, add them to the transactions log!
                        val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                            it[SonhosTransactionsLog.user] = winner.value.toLong()
                            it[SonhosTransactionsLog.timestamp] = now
                        }

                        CoinFlipBetGlobalSonhosTransactionsLog.insert {
                            it[CoinFlipBetGlobalSonhosTransactionsLog.timestampLog] = winnerTransactionLogId
                            it[CoinFlipBetGlobalSonhosTransactionsLog.matchmakingResult] = resultId
                        }

                        val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                            it[SonhosTransactionsLog.user] = loser.value.toLong()
                            it[SonhosTransactionsLog.timestamp] = now
                        }

                        CoinFlipBetGlobalSonhosTransactionsLog.insert {
                            it[CoinFlipBetGlobalSonhosTransactionsLog.timestampLog] = loserTransactionLogId
                            it[CoinFlipBetGlobalSonhosTransactionsLog.matchmakingResult] = resultId
                        }

                        // Then add/remove the sonhos of the users
                        // Add sonhos to the winner
                        Profiles.update({ Profiles.id eq winner.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money + quantityAfterTax)
                            }
                        }

                        // Remove sonhos of the loser
                        Profiles.update({ Profiles.id eq loser.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money - quantityAfterTax)
                            }
                        }
                    }

                    results.add(
                        CoinFlipResult(
                            winner,
                            loser,
                            isTails,
                            otherUserId,
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken],
                            quantity,
                            quantityAfterTax,
                            tax,
                            taxPercentage,
                            premiumUsers
                        )
                    )

                    // Check achievements
                    val giveOutSevenSequentiallyWinsAchievementToWinner = CoinFlipBetGlobalMatchmakingResults.select {
                        CoinFlipBetGlobalMatchmakingResults.winner eq winner.value.toLong() or (CoinFlipBetGlobalMatchmakingResults.loser eq winner.value.toLong())
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)
                        .limit(7)
                        .toList()
                        .let {
                            if (it.size != 7)
                                false
                            else
                                it.all { it[CoinFlipBetGlobalMatchmakingResults.winner].value == winner.value.toLong() }
                        }

                    val giveOutSevenSequentiallyLossesAchievementToLoser = CoinFlipBetGlobalMatchmakingResults.select {
                        CoinFlipBetGlobalMatchmakingResults.winner eq loser.value.toLong() or (CoinFlipBetGlobalMatchmakingResults.loser eq loser.value.toLong())
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)
                        .limit(7)
                        .toList()
                        .let {
                            if (it.size != 7)
                                false
                            else
                                it.all { it[CoinFlipBetGlobalMatchmakingResults.loser].value == loser.value.toLong() }
                        }

                    val now24HoursAgo = Instant.now().minusMillis(24.hours.inWholeMilliseconds)

                    val giveOutFiveHundredMatchesAchievementToWinner = CoinFlipBetGlobalMatchmakingResults.select {
                        CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq now24HoursAgo and (CoinFlipBetGlobalMatchmakingResults.winner eq winner.value.toLong() or (CoinFlipBetGlobalMatchmakingResults.loser eq winner.value.toLong()))
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)
                        .count() >= 500

                    val giveOutFiveHundredMatchesAchievementToLoser = CoinFlipBetGlobalMatchmakingResults.select {
                        CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq now24HoursAgo and (CoinFlipBetGlobalMatchmakingResults.winner eq loser.value.toLong() or (CoinFlipBetGlobalMatchmakingResults.loser eq loser.value.toLong()))
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)
                        .count() >= 500

                    fun giveOutAchievementToUser(userThatWillReceiveTheAchievement: UserId, achievementType: AchievementType) {
                        if (userThatWillReceiveTheAchievement == userId) {
                            results.add(SelfUserAchievementResult(achievementType))
                        } else {
                            results.add(
                                OtherUserAchievementResult(
                                    achievementType,
                                    userThatWillReceiveTheAchievement,
                                    anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken]
                                )
                            )
                        }
                    }

                    giveOutAchievementToUser(winner, AchievementType.COIN_FLIP_BET_WIN)
                    giveOutAchievementToUser(loser, AchievementType.COIN_FLIP_BET_LOSE)

                    if (giveOutSevenSequentiallyWinsAchievementToWinner)
                        giveOutAchievementToUser(winner, AchievementType.COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS)

                    if (giveOutSevenSequentiallyLossesAchievementToLoser)
                        giveOutAchievementToUser(loser, AchievementType.COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS)

                    if (giveOutFiveHundredMatchesAchievementToWinner)
                        giveOutAchievementToUser(winner, AchievementType.COIN_FLIP_BET_PROFESSIONAL)

                    if (giveOutFiveHundredMatchesAchievementToLoser)
                        giveOutAchievementToUser(loser, AchievementType.COIN_FLIP_BET_PROFESSIONAL)

                    return@transaction results
                }
            }

            CoinFlipBetGlobalMatchmakingQueue.insert {
                it[CoinFlipBetGlobalMatchmakingQueue.user] = profile.id.value.toLong()
                it[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken] = userInteractionToken
                it[CoinFlipBetGlobalMatchmakingQueue.quantity] = quantity
                it[CoinFlipBetGlobalMatchmakingQueue.timestamp] = now
                it[CoinFlipBetGlobalMatchmakingQueue.expiresAt] = now.plusMillis(300_000)
            }

            return@transaction results.apply { add(AddedToQueueResult()) }
        }
    }

    private fun _cleanUpMatchmakingQueue() {
        // Clean up matchmaking queue
        val now = Instant.now()

        CoinFlipBetGlobalMatchmakingQueue.deleteWhere {
            CoinFlipBetGlobalMatchmakingQueue.expiresAt less now
        }
    }

    sealed class CoinFlipGlobalMatchmakingResult

    class AlreadyInQueueResult : CoinFlipGlobalMatchmakingResult()
    class AddedToQueueResult : CoinFlipGlobalMatchmakingResult()
    class CoinFlipResult(
        val winner: UserId,
        val loser: UserId,
        val isTails: Boolean,
        val otherUser: UserId,
        val userInteractionToken: String,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?,
        val premiumUsers: List<UserId>
    ) : CoinFlipGlobalMatchmakingResult()
    class YouDontHaveEnoughSonhosToBetResult : CoinFlipGlobalMatchmakingResult()
    class AnotherUserRemovedFromMatchmakingQueueResult(
        val user: UserId,
        val userInteractionToken: String
    ) : CoinFlipGlobalMatchmakingResult()
    class SelfUserAchievementResult(
        val achievementType: AchievementType
    ) : CoinFlipGlobalMatchmakingResult()
    class OtherUserAchievementResult(
        val achievementType: AchievementType,
        val user: UserId,
        val userInteractionToken: String
    ) : CoinFlipGlobalMatchmakingResult()

    class UserCoinFlipBetGlobalStats(
        val winCount: Long,
        val lostCount: Long,
        val winSum: Long,
        val lostSum: Long
    )

    open class CoinFlipBetGlobalMatchmakingQuantityStats(
        val averageTimeOnQueue: Duration?,
        val recentMatches: Long,
        val playersPresentInMatchmakingQueue: Boolean,
    )

    class UserSpecificCoinFlipBetGlobalMatchmakingQuantityStats(
        averageTimeOnQueue: Duration?,
        recentMatches: Long,
        playersPresentInMatchmakingQueue: Boolean,
        val userPresentInMatchmakingQueue: Boolean
    ) : CoinFlipBetGlobalMatchmakingQuantityStats(
        averageTimeOnQueue,
        recentMatches,
        playersPresentInMatchmakingQueue
    )
}