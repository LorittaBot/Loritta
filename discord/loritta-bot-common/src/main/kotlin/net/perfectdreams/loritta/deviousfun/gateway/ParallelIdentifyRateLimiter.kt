package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.ratelimit.RateLimiter
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.upsert
import net.perfectdreams.loritta.cinnamon.pudding.tables.ConcurrentLoginBuckets
import net.perfectdreams.loritta.deviouscache.requests.LockConcurrentLoginRequest
import net.perfectdreams.loritta.deviouscache.responses.LockSuccessfulConcurrentLoginResponse
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Implements rate limiting based on Discord's "sharding for very large bots", using Redis.
 *
 * https://redis.io/docs/reference/patterns/distributed-locks/
 * https://discord.com/developers/docs/topics/gateway#sharding-for-large-bots
 */
class ParallelIdentifyRateLimiter(
    private val loritta: LorittaBot,
    private val shardId: Int,
    val bucketId: Int
) : RateLimiter {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val random = Random.Default
    var currentRandomKey: String? = null

    override suspend fun consume() {
        // PostgreSQL should handle conflicts by itself, so if two instances try to edit the same column at the same time, a concurrent modification exception will happen
        // If randomKey == null, then this bucket is already being used
        // If randomKey != null, then this bucket isn't being used
        val randomKey = loritta.newSuspendedTransaction {
            val currentStatus = ConcurrentLoginBuckets.select {
                ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.lockedAt greaterEq Instant.now().minusSeconds(60))
            }.firstOrNull()

            if (currentStatus != null) {
                return@newSuspendedTransaction null
            } else {
                val newRandomKey = Base64.getEncoder().encodeToString(random.nextBytes(20))

                ConcurrentLoginBuckets.upsert(ConcurrentLoginBuckets.id) {
                    it[ConcurrentLoginBuckets.id] = bucketId
                    it[ConcurrentLoginBuckets.randomKey] = newRandomKey
                    it[ConcurrentLoginBuckets.lockedAt] = Instant.now()
                }

                return@newSuspendedTransaction newRandomKey
            }
        }

        if (randomKey != null) {
            // Acquired lock! We can login, yay!! :3
            this.currentRandomKey = randomKey

            logger.info { "Successfully acquired lock for bucket $bucketId (shard $shardId)!" }
        } else {
            // Couldn't acquire lock, let's wait...
            logger.info { "Couldn't acquire lock for bucket $bucketId (shard $shardId), let's wait and try again later..." }
            delay(1_000)
            // And try again!
            return consume()
        }
    }
}