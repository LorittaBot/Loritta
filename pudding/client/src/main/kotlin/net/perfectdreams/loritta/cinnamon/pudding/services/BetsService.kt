package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingQueue
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.serializable.StoredCoinFlipBetGlobalTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.Duration
import java.time.Instant
import kotlin.math.min
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

            val averageTimeOnQueueData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.quantity, avgTimeOnQueueField)
                .where {
                    CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
                }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val recentMatchesData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.quantity, quantityCount).where { 
                CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val playerPresentInMatchmakingQueueData = CoinFlipBetGlobalMatchmakingQueue.select(CoinFlipBetGlobalMatchmakingQueue.quantity).where { 
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

            val averageTimeOnQueueData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.quantity, avgTimeOnQueueField)
                .where {
                    CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
                }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val recentMatchesData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.quantity, quantityCount).where { 
                CoinFlipBetGlobalMatchmakingResults.quantity inList quantities and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.groupBy(CoinFlipBetGlobalMatchmakingResults.quantity)

            val playerPresentInMatchmakingQueue = CoinFlipBetGlobalMatchmakingQueue.select(CoinFlipBetGlobalMatchmakingQueue.quantity).where { 
                CoinFlipBetGlobalMatchmakingQueue.quantity inList quantities
            }.groupBy(CoinFlipBetGlobalMatchmakingQueue.quantity)

            val currentPlayerPresentInMatchmakingQueue = CoinFlipBetGlobalMatchmakingQueue.select(CoinFlipBetGlobalMatchmakingQueue.quantity).where { 
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

            val winCount = CoinFlipBetGlobalMatchmakingResults.select(sumField).where { 
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.count()

            val lostCount = CoinFlipBetGlobalMatchmakingResults.select(sumField).where { 
                (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }.count()

            val winSum = CoinFlipBetGlobalMatchmakingResults.select(sumField).selectFirstOrNull {
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }?.getOrNull(sumField) ?: 0L

            val lostSum = CoinFlipBetGlobalMatchmakingResults.select(sumField).selectFirstOrNull {
                (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong) and (CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq javaCutoff)
            }?.getOrNull(sumField) ?: 0L

            return@transaction UserCoinFlipBetGlobalStats(
                winCount,
                lostCount,
                winSum,
                lostSum
            )
        }
    }

    suspend fun getCoinFlipBetGlobalUserWinningStreakStats(
        userId: UserId
    ): Int {
        val userAsLong = userId.value.toLong()

        return pudding.transaction {
            val userMatchmakingData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.winner, CoinFlipBetGlobalMatchmakingResults.loser, CoinFlipBetGlobalMatchmakingResults.timestamp).where { 
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) or (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong)
            }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)

            var streakCount = 0

            for (data in userMatchmakingData) {
                if (data[CoinFlipBetGlobalMatchmakingResults.winner].value != userAsLong)
                    break

                streakCount++
            }

            return@transaction streakCount
        }
    }

    suspend fun getCoinFlipBetGlobalUserLosingStreakStats(
        userId: UserId
    ): Int {
        val userAsLong = userId.value.toLong()

        return pudding.transaction {
            val userMatchmakingData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.winner, CoinFlipBetGlobalMatchmakingResults.loser, CoinFlipBetGlobalMatchmakingResults.timestamp).where { 
                (CoinFlipBetGlobalMatchmakingResults.winner eq userAsLong) or (CoinFlipBetGlobalMatchmakingResults.loser eq userAsLong)
            }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)

            var streakCount = 0

            for (data in userMatchmakingData) {
                if (data[CoinFlipBetGlobalMatchmakingResults.loser].value != userAsLong)
                    break

                streakCount++
            }

            return@transaction streakCount
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
        userLanguage: String,
        quantity: Long
    ): List<CoinFlipGlobalMatchmakingResult> {
        return pudding.transaction {
            val now = Instant.now()
            _cleanUpMatchmakingQueue()

            // Used for matchmaking results, because some responses may have multiple results
            // (Example: if someone joins the matchmaking queue but doesn't have enough sonhos for it)
            val results = mutableListOf<CoinFlipGlobalMatchmakingResult>()

            // Get if the user is already on the matchmaking queue for the current quantity
            val selfUserMatchmakingQueueCount = CoinFlipBetGlobalMatchmakingQueue.selectAll().where {
                CoinFlipBetGlobalMatchmakingQueue.user eq userId.value.toLong() and (CoinFlipBetGlobalMatchmakingQueue.quantity eq quantity)
            }.count()

            if (selfUserMatchmakingQueueCount != 0L)
                return@transaction results.apply { add(AlreadyInQueueResult()) }

            // If not, we are going to check if there is anyone else on the matchmaking queue that isn't ourselves...
            val anotherUserMatchmakingData = CoinFlipBetGlobalMatchmakingQueue.selectFirstOrNull {
                CoinFlipBetGlobalMatchmakingQueue.user neq userId.value.toLong() and (CoinFlipBetGlobalMatchmakingQueue.quantity eq quantity)
            }

            // Create self profile
            val profile = pudding.users.getOrCreateUserProfile(userId)

            if (quantity > profile.money)
                return@transaction results.apply { add(YouDontHaveEnoughSonhosToBetResult()) }

            if (anotherUserMatchmakingData != null) {
                // Check if both users have enough sonhos
                val anotherUserProfile = pudding.users.getOrCreateUserProfile(
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
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken],
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.language]
                        )
                    )
                } else {
                    val otherUserId = UserId(anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.user].value)

                    // Do matchmaking stuff
                    CoinFlipBetGlobalMatchmakingQueue.deleteWhere {
                        CoinFlipBetGlobalMatchmakingQueue.id eq anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.id]
                    }

                    // Check if any of the two users are premium users
                    val selfActiveDonations = pudding.payments.getActiveMoneyFromDonations(userId)
                    val otherUserActiveDonations = pudding.payments.getActiveMoneyFromDonations(otherUserId)

                    val selfUserPremiumPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
                    val otherUserPremiumPlan = UserPremiumPlans.getPlanFromValue(otherUserActiveDonations)

                    val premiumUsers = mutableListOf<UserId>()
                    if (!selfUserPremiumPlan.isCoinFlipBetRewardTaxed)
                        premiumUsers.add(userId)
                    if (!otherUserPremiumPlan.isCoinFlipBetRewardTaxed)
                        premiumUsers.add(otherUserId)

                    // If there is someone in the premiumUsers list, then it means that it is a non taxed match!
                    val noSonhosTax = premiumUsers.isNotEmpty()

                    var taxPercentage = min(selfUserPremiumPlan.coinFlipRewardTax, otherUserPremiumPlan.coinFlipRewardTax)

                    val tax: Long?
                    val quantityAfterTax: Long

                    if (noSonhosTax) {
                        tax = null
                        quantityAfterTax = quantity
                        // This is actually not needed, because the variable should be 0.0 at this point
                        taxPercentage = 0.0
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

                    val winnerAsLong = winner.value.toLong()
                    val loserAsLong = loser.value.toLong()

                    val resultId = CoinFlipBetGlobalMatchmakingResults.insertAndGetId {
                        it[CoinFlipBetGlobalMatchmakingResults.winner] = winnerAsLong
                        it[CoinFlipBetGlobalMatchmakingResults.loser] = loserAsLong
                        it[CoinFlipBetGlobalMatchmakingResults.quantity] = quantity
                        it[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax] = quantityAfterTax
                        it[CoinFlipBetGlobalMatchmakingResults.tax] = tax
                        it[CoinFlipBetGlobalMatchmakingResults.taxPercentage] = taxPercentage
                        it[CoinFlipBetGlobalMatchmakingResults.timestamp] = now
                        it[CoinFlipBetGlobalMatchmakingResults.timeOnQueue] = Duration.between(anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.timestamp], now)
                    }

                    if (quantity != 0L) {
                        // If the quantity is not zero, add them to the transactions log!
                        // Cinnamon transaction log
                        SimpleSonhosTransactionsLogUtils.insert(
                            winnerAsLong,
                            now,
                            TransactionType.COINFLIP_BET_GLOBAL,
                            quantityAfterTax,
                            StoredCoinFlipBetGlobalTransaction(resultId.value)
                        )

                        SimpleSonhosTransactionsLogUtils.insert(
                            loserAsLong,
                            now,
                            TransactionType.COINFLIP_BET_GLOBAL,
                            quantity,
                            StoredCoinFlipBetGlobalTransaction(resultId.value)
                        )

                        // Then add/remove the sonhos of the users
                        // Add sonhos to the winner
                        // The winner should receive the after tax amount
                        Profiles.update({ Profiles.id eq winner.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money + quantityAfterTax)
                            }
                        }

                        // Remove sonhos of the loser
                        // The loser should lose the full quantity, before taxes
                        Profiles.update({ Profiles.id eq loser.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money - quantity)
                            }
                        }
                    }

                    // Get the win/lose streak of both users
                    val winnerUserMatchmakingData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.winner, CoinFlipBetGlobalMatchmakingResults.loser, CoinFlipBetGlobalMatchmakingResults.timestamp).where { 
                        (CoinFlipBetGlobalMatchmakingResults.winner eq winnerAsLong) or (CoinFlipBetGlobalMatchmakingResults.loser eq winnerAsLong)
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)

                    var winnerStreakCount = 0

                    for (data in winnerUserMatchmakingData) {
                        if (data[CoinFlipBetGlobalMatchmakingResults.winner].value != winnerAsLong)
                            break

                        winnerStreakCount++
                    }

                    val loserUserMatchmakingData = CoinFlipBetGlobalMatchmakingResults.select(CoinFlipBetGlobalMatchmakingResults.winner, CoinFlipBetGlobalMatchmakingResults.loser, CoinFlipBetGlobalMatchmakingResults.timestamp).where { 
                        (CoinFlipBetGlobalMatchmakingResults.winner eq loserAsLong) or (CoinFlipBetGlobalMatchmakingResults.loser eq loserAsLong)
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)

                    var loserStreakCount = 0

                    for (data in loserUserMatchmakingData) {
                        if (data[CoinFlipBetGlobalMatchmakingResults.loser].value != loserAsLong)
                            break

                        loserStreakCount++
                    }

                    results.add(
                        CoinFlipResult(
                            winner,
                            loser,
                            isTails,
                            otherUserId,
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken],
                            anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.language],
                            quantity,
                            quantityAfterTax,
                            tax,
                            taxPercentage,
                            premiumUsers,
                            winnerStreakCount,
                            loserStreakCount
                        )
                    )

                    // Check achievements
                    val giveOutSevenSequentiallyWinsAchievementToWinner = CoinFlipBetGlobalMatchmakingResults.selectAll().where {
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

                    val giveOutSevenSequentiallyLossesAchievementToLoser = CoinFlipBetGlobalMatchmakingResults.selectAll().where {
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

                    val giveOutFiveHundredMatchesAchievementToWinner = CoinFlipBetGlobalMatchmakingResults.selectAll().where {
                        CoinFlipBetGlobalMatchmakingResults.timestamp greaterEq now24HoursAgo and (CoinFlipBetGlobalMatchmakingResults.winner eq winner.value.toLong() or (CoinFlipBetGlobalMatchmakingResults.loser eq winner.value.toLong()))
                    }.orderBy(CoinFlipBetGlobalMatchmakingResults.timestamp, SortOrder.DESC)
                        .count() >= 500

                    val giveOutFiveHundredMatchesAchievementToLoser = CoinFlipBetGlobalMatchmakingResults.selectAll().where {
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
                                    anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.userInteractionToken],
                                    anotherUserMatchmakingData[CoinFlipBetGlobalMatchmakingQueue.language]
                                )
                            )
                        }
                    }

                    giveOutAchievementToUser(winner, AchievementType.COIN_FLIP_BET_WIN)
                    giveOutAchievementToUser(loser, AchievementType.COIN_FLIP_BET_LOSE)

                    if (giveOutSevenSequentiallyWinsAchievementToWinner)
                        giveOutAchievementToUser(winner, AchievementType.COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS)

                    if (giveOutSevenSequentiallyLossesAchievementToLoser)
                        giveOutAchievementToUser(loser, AchievementType.COIN_FLIP_BET_SEVEN_SEQUENTIAL_LOSSES)

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
                it[CoinFlipBetGlobalMatchmakingQueue.language] = userLanguage
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
        val otherUserLanguage: String,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?,
        val premiumUsers: List<UserId>,
        val winnerStreakCount: Int,
        val loserStreakCount: Int
    ) : CoinFlipGlobalMatchmakingResult()
    class YouDontHaveEnoughSonhosToBetResult : CoinFlipGlobalMatchmakingResult()
    class AnotherUserRemovedFromMatchmakingQueueResult(
        val user: UserId,
        val userInteractionToken: String,
        val language: String
    ) : CoinFlipGlobalMatchmakingResult()
    class SelfUserAchievementResult(
        val achievementType: AchievementType
    ) : CoinFlipGlobalMatchmakingResult()
    class OtherUserAchievementResult(
        val achievementType: AchievementType,
        val user: UserId,
        val userInteractionToken: String,
        val language: String
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