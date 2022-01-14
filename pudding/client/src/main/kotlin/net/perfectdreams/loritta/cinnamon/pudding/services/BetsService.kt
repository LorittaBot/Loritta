package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalMatchmakingQueue
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.Instant

class BetsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun addToMatchmakingQueue(
        userId: UserId,
        userInteractionToken: String,
        quantity: Long
    ): List<CoinflipGlobalMatchmakingResult> {
        return pudding.transaction {
            val results = mutableListOf<CoinflipGlobalMatchmakingResult>()

            val selfUserMatchmakingQueueCount = CoinflipGlobalMatchmakingQueue.select {
                CoinflipGlobalMatchmakingQueue.user eq userId.value.toLong()
            }.count()

            if (selfUserMatchmakingQueueCount != 0L)
                return@transaction results.apply { add(AlreadyInQueueResult()) }

            val anotherUserMatchmakingData = CoinflipGlobalMatchmakingQueue.select {
                CoinflipGlobalMatchmakingQueue.user neq userId.value.toLong() and (CoinflipGlobalMatchmakingQueue.quantity eq quantity)
            }.firstOrNull()

            // Create self profile
            val profile = pudding.users._getOrCreateUserProfile(userId)

            if (quantity > profile.money)
                return@transaction results.apply { add(YouDontHaveEnoughSonhosToBetResult()) }

            if (anotherUserMatchmakingData != null) {
                // Check if both users have enough sonhos
                val anotherUserProfile = pudding.users._getOrCreateUserProfile(
                    UserId(
                        anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.user].value
                    )
                )

                if (quantity > anotherUserProfile.money) {
                    // The other user doesn't have enough sonhos to participate, let's remove them from the global matchmaking queue and then we will notify that they were removed
                    CoinflipGlobalMatchmakingQueue.deleteWhere {
                        CoinflipGlobalMatchmakingQueue.id eq anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.id]
                    }

                    results.add(
                        AnotherUserRemovedFromMatchmakingQueue(
                            UserId(anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.user].value),
                            anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.userInteractionToken]
                        )
                    )
                } else {
                    val now = Instant.now()

                    // Do matchmaking stuff
                    CoinflipGlobalMatchmakingQueue.deleteWhere {
                        CoinflipGlobalMatchmakingQueue.id eq anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.id]
                    }

                    val players = mutableListOf(
                        userId,
                        UserId(anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.user].value)
                    ).also { it.shuffle() }

                    val winner = players.removeFirst()
                    val loser = players.removeFirst()

                    val resultId = CoinflipGlobalMatchmakingResults.insertAndGetId {
                        it[CoinflipGlobalMatchmakingResults.winner] = winner.value.toLong()
                        it[CoinflipGlobalMatchmakingResults.loser] = loser.value.toLong()
                        it[CoinflipGlobalMatchmakingResults.quantity] = quantity
                        it[CoinflipGlobalMatchmakingResults.timestamp] = now
                        it[CoinflipGlobalMatchmakingResults.timeOnQueue] = Duration.between(anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.timestamp], now)
                    }

                    if (quantity != 0L) {
                        // If the quantity is not zero, add them to the transactions log!
                        val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                            it[SonhosTransactionsLog.user] = winner.value.toLong()
                            it[SonhosTransactionsLog.timestamp] = now
                        }

                        CoinflipGlobalSonhosTransactionsLog.insert {
                            it[CoinflipGlobalSonhosTransactionsLog.timestampLog] = winnerTransactionLogId
                            it[CoinflipGlobalSonhosTransactionsLog.matchmakingResult] = resultId
                        }

                        val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                            it[SonhosTransactionsLog.user] = loser.value.toLong()
                            it[SonhosTransactionsLog.timestamp] = now
                        }

                        CoinflipGlobalSonhosTransactionsLog.insert {
                            it[CoinflipGlobalSonhosTransactionsLog.timestampLog] = loserTransactionLogId
                            it[CoinflipGlobalSonhosTransactionsLog.matchmakingResult] = resultId
                        }

                        // Then add/remove the sonhos of the users
                        // Add sonhos to the winner
                        Profiles.update({ Profiles.id eq winner.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money + quantity)
                            }
                        }

                        // Remove sonhos of the loser
                        Profiles.update({ Profiles.id eq loser.value.toLong() }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money - quantity)
                            }
                        }
                    }

                    results.add(
                        CoinflipResult(
                            winner,
                            loser,
                            UserId(anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.user].value),
                            anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.userInteractionToken]
                        )
                    )
                    return@transaction results
                }
            }

            CoinflipGlobalMatchmakingQueue.insert {
                it[CoinflipGlobalMatchmakingQueue.user] = profile.id.value.toLong()
                it[CoinflipGlobalMatchmakingQueue.userInteractionToken] = userInteractionToken
                it[CoinflipGlobalMatchmakingQueue.quantity] = quantity
                it[CoinflipGlobalMatchmakingQueue.timestamp] = Instant.now()
            }

            return@transaction results.apply { add(AddedToQueueResult()) }
        }
    }

    sealed class CoinflipGlobalMatchmakingResult

    class AlreadyInQueueResult : CoinflipGlobalMatchmakingResult()
    class AddedToQueueResult : CoinflipGlobalMatchmakingResult()
    class CoinflipResult(
        val winner: UserId,
        val loser: UserId,
        val otherUser: UserId,
        val userInteractionToken: String
    ) : CoinflipGlobalMatchmakingResult()
    class YouDontHaveEnoughSonhosToBetResult : CoinflipGlobalMatchmakingResult()
    class AnotherUserRemovedFromMatchmakingQueue(
        val user: UserId,
        val userInteractionToken: String
    ) : CoinflipGlobalMatchmakingResult()
}