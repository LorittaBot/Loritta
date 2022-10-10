package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.ratelimit.RateLimiter
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import redis.clients.jedis.params.SetParams
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
    private val bucketId: Int
) : RateLimiter {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val random = Random.Default

    override suspend fun consume() {
        val randomKey = random.nextBytes(20).toString(Charsets.UTF_8)

        val success = loritta.redisConnection {
            it.set(
                loritta.redisKeys.discordGatewayConcurrentLogin(bucketId),
                randomKey,
                SetParams.setParams()
                    .nx()
                    // max_concurrency = Number of identify requests allowed per 5 seconds
                    // So the key must live for 5 seconds
                    .px(5_000)
            )
        } != null

        if (success) {
            // Acquired lock! We can login, yay!! :3
            logger.info { "Successfully acquired lock for bucket $bucketId (shard $shardId)!" }
        } else {
            // Couldn't acquire lock, let's wait...
            logger.info { "Couldn't acquire lock for bucket $bucketId (shard $shardId), let's wait and try again later..." }
            delay(5_000)
            // And try again!
            return consume()
        }
    }
}