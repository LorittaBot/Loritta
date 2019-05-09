package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong

class RequestLimiter(val loritta: Loritta) {
    companion object {
        // Hacky, hacky, hacky!!!
        private val REQUESTER_FIELD = net.dv8tion.jda.internal.JDAImpl::class.java.getDeclaredField("requester").apply {
            isAccessible = true
        }
        private val RATE_LIMITER_FIELD = net.dv8tion.jda.internal.requests.Requester::class.java.getDeclaredField("rateLimiter").apply {
            isAccessible = true
        }

        private val logger = KotlinLogging.logger {}
    }

    val ignoreRequestsUntil = AtomicLong()

    fun isRateLimited(): Boolean {
        if (!loritta.discordConfig.discord.requestLimiter.enabled)
            return false

        if (ignoreRequestsUntil.get() > System.currentTimeMillis())
            return true

        val pendingRequests = getPendingRequests()

        if (loritta.discordConfig.discord.requestLimiter.maxRequests >= pendingRequests)
            return false

        // Rate limited
        logger.warn { "Number of pending requests ($pendingRequests requests) is higher than the max request threshold (${loritta.discordConfig.discord.requestLimiter.maxRequests} requests)! Ignoring requests for ${loritta.discordConfig.discord.requestLimiter.ignoreRequestsFor} milliseconds!" }
        ignoreRequestsUntil.set(System.currentTimeMillis() + loritta.discordConfig.discord.requestLimiter.ignoreRequestsFor)
        return true
    }

    fun getPendingRequests(): Int {
        var rateLimitedRequests = 0

        for (jda in lorittaShards.shardManager.shards) {
            val requester = REQUESTER_FIELD.get(jda)
            val limiter = RATE_LIMITER_FIELD.get(requester) as net.dv8tion.jda.internal.requests.ratelimit.BotRateLimiter

            val buckets = limiter.routeBuckets
            val subBuck = limiter.queuedRouteBuckets

            rateLimitedRequests += (buckets.sumBy { it.requests.size }) + (subBuck.sumBy { it.requests.size })
        }

        return rateLimitedRequests
    }
}