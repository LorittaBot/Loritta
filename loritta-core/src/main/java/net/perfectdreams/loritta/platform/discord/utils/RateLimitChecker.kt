package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RateLimiter
import net.dv8tion.jda.internal.requests.Requester

class RateLimitChecker(val m: Loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}

		val requesterField by lazy {
			val field = JDAImpl::class.java.getDeclaredField("requester")
			field.isAccessible = true
			field
		}

		val rateLimiterField by lazy {
			val field = Requester::class.java.getDeclaredField("rateLimiter")
			field.isAccessible = true
			field
		}

		fun changeRateLimiterToCustomRateLimiter(jda: JDA) {
			val requester = requesterField.get(jda) as Requester
			val customRateLimiter = LoriBotRateLimiter(requester)
			rateLimiterField.set(requester, customRateLimiter)
			logger.info { "JDA instance $jda ratelimiter was changed to $customRateLimiter!" }
		}

		fun getRateLimiter(jda: JDA): LoriBotRateLimiter {
			val requester = requesterField.get(jda) as Requester
			val customRateLimiter = LoriBotRateLimiter(requester)
			val rateLimiter = rateLimiterField.get(customRateLimiter) as RateLimiter

			if (rateLimiter !is LoriBotRateLimiter)
				throw RuntimeException("JDA instance $jda is not using LoriBotRateLimiter!")

			return rateLimiter
		}
	}

	val maxRequestsPer10Minutes =  20_000 / m.config.clusters.size
	var lastRequestWipe = System.currentTimeMillis()

	fun getAllPendingRequests() = lorittaShards.shardManager.shards.flatMap {
		logger.info { "Cancelling pending requests in shard $it" }
		val rateLimiter = getRateLimiter(it)
		rateLimiter.getAllRequests()
	}

	fun checkIfRequestShouldBeIgnored(): Boolean {
		// https://i.imgur.com/crENfcG.png
		// Um bot pode fazer 25k requests inválidos em 10 minutos
		// O limite é 25k https://cdn.discordapp.com/attachments/409847691896422410/672573213284237312/unknown.png
		// Para calcular, vamos fazer que seja (25k / número de clusters)
		// Mas para a gente não ficar muito "em cima do muro", vamos colocar (20k / número de clusters)
		val rateLimitHits = m.bucketedController?.getGlobalRateLimitHitsInTheLastMinute() ?: 0
		val shouldIgnore = rateLimitHits >= maxRequestsPer10Minutes

		if (shouldIgnore) {
			logger.warn { "All received events are cancelled and ignored due to too many global ratelimitted requests being sent! $rateLimitHits >= $maxRequestsPer10Minutes" }
			val diff = System.currentTimeMillis() - lastRequestWipe

			if (diff >= 60_000) {
				logger.info { "Cancelling all pending requests for all shards!" }
				// Limpar todos os requests pendentes
				lorittaShards.shardManager.shards.forEach {
					logger.info { "Cancelling pending requests in shard $it" }
					val rateLimiter = getRateLimiter(it)
					rateLimiter.cancelAllRequests()
				}
				this.lastRequestWipe = System.currentTimeMillis()
			}
		}

		return shouldIgnore
	}
}