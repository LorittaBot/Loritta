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
    val bucketId: Int
) : RateLimiter {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val random = Random.Default
    var currentRandomKey: String? = null

    override suspend fun consume() {
        val randomKey = random.nextBytes(20).toString(Charsets.UTF_8)
        this.currentRandomKey = randomKey

        val success = loritta.redisConnection("locking concurrent login of bucket $bucketId") {
            it.set(
                loritta.redisKeys.discordGatewayConcurrentLogin(bucketId),
                randomKey,
                SetParams.setParams()
                    .nx()
                    // A shard *probably* won't take more than 60s to receive its Ready event, but let's make it expire after 60s to avoid the bucket being blocked if we had any issues while logging in.
                    .px(60_000)
            )
        } != null

        if (success) {
            // Acquired lock! We can login, yay!! :3
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