package net.perfectdreams.loritta.deviousfun.requests

import dev.kord.rest.ratelimit.*
import dev.kord.rest.request.Request
import dev.kord.rest.request.RequestIdentifier
import dev.kord.rest.request.identifier
import dev.kord.rest.route.Route
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import kotlin.time.Duration


/**
 * A Rate Limiter implementation (kind of) based off JDA's implementation, because Discord's Rate Limiting docs are so confusing
 *
 * https://github.com/DV8FromTheWorld/JDA/blob/f181320b100ff5ff6934a5147bc913de54910835/src/main/java/net/dv8tion/jda/internal/requests/ratelimit/BotRateLimiter.java#L32
 */
// basically bucket + route + major params => rate limit spec (x requests in y seconds allowed)
// rate limit spec + major params => individual rate limit (what you see in remaining)
// https://canary.discord.com/channels/613425648685547541/697489244649816084/1039525963387646086
//
// https://canary.discord.com/channels/613425648685547541/801247546151403531/987104918458470512
//
// https://github.com/hikari-py/hikari/blob/master/hikari/impl/buckets.py#L51-L69
//
// arviq — 03/03/2022
// I'm trying to write good code for preventing 429s, but I don't see any practical use for X-RateLimit-Bucket header.
// Does it provide anything useful?
// advaith — 03/03/2022
// I think you're supposed to pause requests to all endpoints in the bucket
// many libs just ignore it tho
// Velvet — 03/03/2022
// bucket ➜ limit mapping?
// Other than than :kioShrug:
// https://canary.discord.com/channels/613425648685547541/801247546151403531/949043656441086003
class DeviousRateLimiter(val clock: Clock) {
    companion object {
        private val logger = KotlinLogging.logger {}

        const val RESET_AFTER_HEADER = "X-RateLimit-Reset-After"
        const val RESET_HEADER = "X-RateLimit-Reset"
        const val LIMIT_HEADER = "X-RateLimit-Limit"
        const val REMAINING_HEADER = "X-RateLimit-Remaining"
        const val GLOBAL_HEADER = "X-RateLimit-Global"
        const val HASH_HEADER = "X-RateLimit-Bucket"
        const val RETRY_AFTER_HEADER = "Retry-After"
    }

    // Because creating/getting buckets are lightweight, we don't need to care about concurrency, so we will use Mutex to synchronize access
    private val bucketModificationMutex = Mutex()
    private val globalRateLimitMutex = Mutex()
    // This is only set when using updateRateLimitStatus
    private val routeToBucketHash = mutableMapOf<Route<*>, String>()
    private val bucketIdToBucket = mutableMapOf<String, Bucket>()

    suspend fun updateRateLimitStatus(request: Request<*, *>, response: HttpResponse) {
        // First we need to figure out what bucket should we put this request
        // If the X-RateLimit-Bucket header is not present, the "unlimited+method:routePath" key will be used
        bucketModificationMutex.withLock {
            val discordBucketHash = response.headers[HASH_HEADER]
            val bucketHash = discordBucketHash ?: getUnlimitedBucketHash(request)

            if (!routeToBucketHash.containsKey(request.route)) {
                logger.debug { "Caching bucket hash $bucketHash for route ${request.route}" }
                // Remove "unlimited" bucket
                bucketIdToBucket.remove(getUnlimitedBucketHash(request))
                routeToBucketHash[request.route] = bucketHash
            }

            val bucket = getOrCreateBucket(bucketHash, request)

            // If hash is null this means we didn't get enough information to update a bucket
            if (discordBucketHash == null)
                return

            // Update the bucket parameters with new information
            val limitHeader = response.headers[LIMIT_HEADER] ?: error("$LIMIT_HEADER is missing from the response!")
            val remainingHeader = response.headers[REMAINING_HEADER] ?: error("$REMAINING_HEADER is missing from the response!")
            val resetAfterHeader = response.headers[RESET_AFTER_HEADER] ?: error("$RESET_AFTER_HEADER is missing from the response!")
            val resetHeader = response.headers[RESET_HEADER] ?: error("$RESET_HEADER is missing from the response!")

            // This MUST be called within the checkAndHoldBucketForRequest call!
            bucket.rateLimitWithReset = RateLimitWithReset(
                RateLimit(
                    Total(limitHeader.toLong().coerceAtLeast(1)),
                    Remaining(remainingHeader.toLong())
                ),
                Reset(Instant.fromEpochMilliseconds((resetHeader.toDouble() * 1000).toLong()))
            )
            logger.trace { "Updated bucket ${bucket.bucketId} to ${bucket.rateLimitWithReset}" }
        }
    }

    // Technically this isn't needed because you can execute requests in parallel, however, to avoid issues, we will execute the requests sequentially based on the bucket ID
    suspend fun <B : Any, R> addRequestToQueue(request: Request<B, R>, action: suspend (Bucket) -> (RateLimitResponse)): HttpResponse {
        while (true) {
            val storedBucketHash: String?
            val bucketHash: String
            val bucket: Bucket

            bucketModificationMutex.withLock {
                storedBucketHash = routeToBucketHash[request.route]
                bucketHash = storedBucketHash ?: getUnlimitedBucketHash(request)
                bucket = getOrCreateBucket(bucketHash, request)
            }

            bucket.executionMutex.withLock {
                // This may happen
                val retryBecauseHashChanged = bucketModificationMutex.withLock {
                    val newHash = routeToBucketHash[request.route]
                    val sameHash = newHash == bucketHash
                    newHash != null && !sameHash
                }

                if (!retryBecauseHashChanged) {
                    // This is only used to check if there is any global rate limit
                    globalRateLimitMutex.withLock {}
                    bucket.awaitRateLimit()

                    when (val result = action.invoke(bucket)) {
                        is RateLimitResponse.CloudFlareRateLimited -> {
                            logger.warn { "Encountered CloudFlare rate limit! Retry-After: ${result.retryAfter}" }
                            val whenRetryAfterWasTriggered = clock.now()
                            val retryAfterFinish = whenRetryAfterWasTriggered + result.retryAfter

                            globalRateLimitMutex.withLock {
                                // We do it like that because multiple parallel requests may hit the rate limit at the first time, and we don't want to use "delay(result.retryAfter)" because this is a mutex
                                delay(clock.now() - retryAfterFinish)
                                logger.info { "CloudFlare rate limit has been lifted!" }
                            }
                            // After unlocking and everything being ok, we will retry the request (finally)
                        }

                        is RateLimitResponse.GloballyRateLimited -> {
                            logger.warn { "Encountered global rate limit! Retry-After: ${result.retryAfter}" }
                            val whenRetryAfterWasTriggered = clock.now()
                            val retryAfterFinish = whenRetryAfterWasTriggered + result.retryAfter

                            globalRateLimitMutex.withLock {
                                // We do it like that because multiple parallel requests may hit the rate limit at the first time, and we don't want to use "delay(result.retryAfter)" because this is a mutex
                                delay(clock.now() - retryAfterFinish)
                                logger.info { "Global rate limit has been lifted!" }
                            }
                            // After unlocking and everything being ok, we will retry the request (finally)
                        }

                        RateLimitResponse.RateLimited -> {} // Do nothing, this will retry the request
                        is RateLimitResponse.Success -> return result.response
                    }
                } else {
                    // If the route has a different hash, then a new bucket has been created for us, and we want to queue the request on the new bucket
                    logger.info { "Bucket hash $bucketHash has changed! Retrying..." }
                }
            }
        }
    }

    private fun getUnlimitedBucketHash(request: Request<*, *>) = "unlimited+${request.route.method.value}:${request.route.path}"

    private fun getBucket(bucketHash: String, request: Request<*, *>) = bucketIdToBucket[getBucketId(bucketHash, request.identifier)]

    private fun getOrCreateBucket(bucketHash: String, request: Request<*, *>): Bucket {
        val bucketId = getBucketId(bucketHash, request.identifier)
        return bucketIdToBucket.getOrPut(bucketId) {
            Bucket(bucketId)
        }
    }

    private fun getBucketId(bucketHash: String, identifier: RequestIdentifier): String {
        var bucketId = bucketHash
        if (identifier is RequestIdentifier.MajorParamIdentifier)
            bucketId += ":${identifier.param}"

        return bucketId
    }

    inner class Bucket(val bucketId: String) {
        var rateLimitWithReset: RateLimitWithReset? = null
        val executionMutex = Mutex()

        suspend fun awaitRateLimit() {
            val rateLimitWithReset = this.rateLimitWithReset
            val rateLimit = rateLimitWithReset?.rateLimit
            val reset = rateLimitWithReset?.reset

            // Is the rate limit null (can be null if the response doesn't have a key, example: emojis) or are we exausted?
            if (rateLimit == null || rateLimit.isExhausted) {
                // Yes, we are, so we need to wait for the rate limit reset!
                if (reset != null) {
                    val duration = reset.value - clock.now()
                    if (!duration.isNegative()) {
                        logger.info { "Bucket $bucketId waiting until ${reset.value} ($duration)" }
                        delay(duration)
                    }
                } else {
                    logger.warn { "Bucket $bucketId is exausted, however we don't have any information about the reset timer" }
                }
            }
        }
    }

    data class RateLimitWithReset(val rateLimit: RateLimit?, val reset: Reset?)

    data class RateLimit(val total: Total, val remaining: Remaining) {
        val isExhausted: Boolean get() = remaining.value == 0L

        public companion object
    }

    @JvmInline
    public value class Reset(public val value: Instant)

    @JvmInline
    public value class Total(public val value: Long)

    @JvmInline
    public value class Remaining(public val value: Long)

    sealed class RateLimitResponse {
        class Success(val response: HttpResponse) : RateLimitResponse()
        object RateLimited : RateLimitResponse()
        class GloballyRateLimited(val retryAfter: Duration) : RateLimitResponse()
        class CloudFlareRateLimited(val retryAfter: Duration) : RateLimitResponse()
    }
}