package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RateLimiter
import net.dv8tion.jda.internal.requests.Requester
import net.dv8tion.jda.internal.requests.ratelimit.BotRateLimiter
import net.dv8tion.jda.internal.requests.ratelimit.IBucket
import java.util.concurrent.ConcurrentHashMap

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

		val bucketsField by lazy {
			val field = BotRateLimiter::class.java.getDeclaredField("buckets")
			field.isAccessible = true
			field
		}

		fun getRateLimiter(jda: JDA): RateLimiter {
			val requester = requesterField.get(jda) as Requester
			val rateLimiter = rateLimiterField.get(requester) as RateLimiter

			return rateLimiter
		}
	}

	var lastRequestWipe = System.currentTimeMillis()
	var lastConsoleWarn = System.currentTimeMillis()

	fun getAllPendingRequests() = lorittaShards.shardManager.shards.flatMap {
		val rateLimiter = getRateLimiter(it)
		val buckets = bucketsField.get(rateLimiter) as ConcurrentHashMap<String, IBucket>
		buckets.flatMap { it.value.requests }
	}

	fun cancelAllPendingRequests() {
		getAllPendingRequests().map { it.cancel() }
	}

	fun checkIfRequestShouldBeIgnored(): Boolean {
		if (!m.discordConfig.discord.requestLimiter.enabled)
			return false

		// https://i.imgur.com/crENfcG.png
		// Um bot pode fazer 25k requests inválidos em 10 minutos
		// O limite é 25k https://cdn.discordapp.com/attachments/409847691896422410/672573213284237312/unknown.png
		// Para calcular, vamos fazer que seja (25k / número de clusters)
		// Mas para a gente não ficar muito "em cima do muro", vamos colocar (20k / número de clusters)
		//
		// O certo era ser 20k, mas parece que o Discord é muito trigger happy com bans, então é melhor deixar um valor
		// extremamente baixo porque ficar alguns minutos sem responder comandos é bem melhor do que ser banido por
		// uma hora
		val rateLimitHits = m.bucketedController?.getGlobalRateLimitHitsInTheLastMinute() ?: 0
		val maxRequestsPer10Minutes = m.discordConfig.discord.requestLimiter.maxRequestsPer10Minutes
		val shouldIgnore = rateLimitHits >= maxRequestsPer10Minutes

		if (shouldIgnore) {
			val diff2 = System.currentTimeMillis() - this.lastConsoleWarn
			if (diff2 >= m.discordConfig.discord.requestLimiter.consoleWarnCooldown) {
				logger.warn { "All received events are cancelled and ignored due to too many global ratelimited requests being sent! $rateLimitHits >= $maxRequestsPer10Minutes" }
				this.lastConsoleWarn = System.currentTimeMillis()
			}

			val diff = System.currentTimeMillis() - lastRequestWipe

			if (diff >= m.discordConfig.discord.requestLimiter.removePendingRequestsCooldown) {
				logger.info { "Cancelling and removing outdated global rate limit hits..." }
				m.bucketedController?.removeOutdatedGlobalRateLimitHits()

				// Limpar todos os requests pendentes
				cancelAllPendingRequests()
				this.lastRequestWipe = System.currentTimeMillis()
			}
		}

		return shouldIgnore
	}
}