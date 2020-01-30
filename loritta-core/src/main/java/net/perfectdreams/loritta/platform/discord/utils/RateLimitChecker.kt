package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.Loritta
import mu.KotlinLogging

class RateLimitChecker(val m: Loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val maxRequestsPer10Minutes =  20_000 / m.config.clusters.size

	fun checkIfRequestShouldBeIgnored(): Boolean {
		// https://i.imgur.com/crENfcG.png
		// Um bot pode fazer 25k requests inválidos em 10 minutos
		// O limite é 25k https://cdn.discordapp.com/attachments/409847691896422410/672573213284237312/unknown.png
		// Para calcular, vamos fazer que seja (25k / número de clusters)
		// Mas para a gente não ficar muito "em cima do muro", vamos colocar (20k / número de clusters)
		val rateLimitHits = m.bucketedController?.getGlobalRateLimitHitsInTheLastMinute() ?: 0
		val shouldIgnore = rateLimitHits >= maxRequestsPer10Minutes

		if (shouldIgnore)
			logger.warn { "All received events are cancelled and ignored due to too many global ratelimitted requests being sent! $rateLimitHits >= $maxRequestsPer10Minutes" }

		return shouldIgnore
	}
}