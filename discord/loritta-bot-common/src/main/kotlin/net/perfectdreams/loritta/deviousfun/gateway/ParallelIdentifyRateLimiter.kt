package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.ratelimit.RateLimiter
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.LockConcurrentLoginRequest
import net.perfectdreams.loritta.deviouscache.responses.LockSuccessfulConcurrentLoginResponse
import net.perfectdreams.loritta.morenitta.LorittaBot
import kotlin.random.Random

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
        val randomKey =
            (loritta.deviousFun.rpc.execute(LockConcurrentLoginRequest(bucketId)) as? LockSuccessfulConcurrentLoginResponse)?.key

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