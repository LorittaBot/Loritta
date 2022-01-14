package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinflipGlobalMatchmakingQueue
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

class BetsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun addToMatchmakingQueue(
        userId: UserId,
        userInteractionToken: String,
        quantity: Long
    ): CoinflipGlobalMatchmakingResult {
        return pudding.transaction {
            val selfUserMatchmakingQueueCount = CoinflipGlobalMatchmakingQueue.select {
                CoinflipGlobalMatchmakingQueue.user eq userId.value.toLong()
            }.count()

            if (selfUserMatchmakingQueueCount != 0L)
                return@transaction AlreadyInQueueResult()

            val anotherUserMatchmakingData = CoinflipGlobalMatchmakingQueue.select {
                CoinflipGlobalMatchmakingQueue.user neq userId.value.toLong() and (CoinflipGlobalMatchmakingQueue.quantity eq quantity)
            }.firstOrNull()

            // Create self profile
            val profile = pudding.users.getOrCreateUserProfile(userId)

            if (anotherUserMatchmakingData != null) {
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

                return@transaction CoinflipResult(
                    winner,
                    loser,
                    UserId(anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.user].value),
                    anotherUserMatchmakingData[CoinflipGlobalMatchmakingQueue.userInteractionToken]
                )
            }

            CoinflipGlobalMatchmakingQueue.insert {
                it[CoinflipGlobalMatchmakingQueue.user] = profile.id.value.toLong()
                it[CoinflipGlobalMatchmakingQueue.userInteractionToken] = userInteractionToken
                it[CoinflipGlobalMatchmakingQueue.quantity] = quantity
                it[CoinflipGlobalMatchmakingQueue.timestamp] = Instant.now()
            }

            return@transaction AddedToQueueResult()
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
}