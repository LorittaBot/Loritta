package net.perfectdreams.loritta.platform.discord.utils

import mu.KotlinLogging
import net.dv8tion.jda.internal.requests.Requester
import net.dv8tion.jda.internal.requests.ratelimit.BotRateLimiter

class LoriBotRateLimiter(requester: Requester) : BotRateLimiter(requester) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	fun getAllRequests() = this.buckets.values.flatMap { it.requests }

	fun cancelAllRequests() {
		val allRequests = getAllRequests()
		logger.info { "All ${allRequests.size} pending requests will be cancelled!" }
		allRequests.onEach { it.cancel() }
	}
}