package com.mrpowergamerbr.loritta.analytics

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.flogger.FluentLogger
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.analytics.AnalyticProcessorService.DISCORD_BOTS
import com.mrpowergamerbr.loritta.analytics.AnalyticProcessorService.DISCORD_BOT_LIST
import com.mrpowergamerbr.loritta.utils.lorittaShards

object LorittaAnalytics {
	private val logger = FluentLogger.forEnclosingClass()

	/**
	 * Sends analytics to the specified processor service
	 *
	 * @param service the analytic processor service
	 */
	fun send(service: AnalyticProcessorService) {
		val request = HttpRequest.post(service.endpoint.format(Loritta.config.clientId))
				.connectTimeout(25000)
				.readTimeout(25000)

		when (service) {
			DISCORD_BOTS -> request.authorization(Loritta.config.discordBotsKey)
			DISCORD_BOT_LIST -> request.authorization(Loritta.config.discordBotsOrgKey)
		}

		request.acceptJson().contentType("application/json")

		val payload = createPayload(service).toString()
		logger.atInfo().log("Sending analytic data to %s - %s", service.name, payload)
		request.send(payload)
		logger.atFine().log("%s: %s", service.name, payload)
	}

	/**
	 * Creates a JSON payload containing analytic data for the analytic processor service
	 *
	 * Each Analytic Processor Service can have different analytic data
	 *
	 * @param service                   the analytic processor service
	 * @throws AnalyticProcessorService if the provided analytic processor isn't supported
	 */
	fun createPayload(service: AnalyticProcessorService): JsonObject {
		when (service) {
			DISCORD_BOTS, DISCORD_BOT_LIST -> {
				return jsonObject(
						"server_count" to lorittaShards.getGuildCount()
				)
			}
			else -> throw UnsupportedAnalyticServiceException()
		}
	}
}