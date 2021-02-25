package com.mrpowergamerbr.loritta.analytics

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.analytics.AnalyticProcessorService.DISCORD_BOTS
import com.mrpowergamerbr.loritta.analytics.AnalyticProcessorService.DISCORD_BOT_LIST
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging

object LorittaAnalytics {
	private val logger = KotlinLogging.logger {}

	/**
	 * Sends analytics to the specified processor service
	 *
	 * @param service    the analytic processor service
	 * @param guildCount current guild count
	 */
	fun send(service: AnalyticProcessorService, guildCount: Int) {
		val request = HttpRequest.post(service.endpoint.format(loritta.discordConfig.discord.clientId))
				.connectTimeout(25_000)
				.readTimeout(25_000)

		when (service) {
			DISCORD_BOTS -> request.authorization(loritta.discordConfig.discordBots.apiKey)
			DISCORD_BOT_LIST -> request.authorization(loritta.discordConfig.discordBotList.apiKey)
		}

		request.acceptJson().contentType("application/json")

		val payload = createPayload(service, guildCount).toString()
		logger.info { "Sending analytic data to ${service.name} - ${payload}" }
		request.send(payload)
		logger.trace { "${service.name}: ${request.body()}" }
		request.ok()
	}

	/**
	 * Creates a JSON payload containing analytic data for the analytic processor service
	 *
	 * Each Analytic Processor Service can have different analytic data
	 *
	 * @param service                   the analytic processor service
	 * @param guildCount                current guild count
	 * @throws AnalyticProcessorService if the provided analytic processor isn't supported
	 */
	fun createPayload(service: AnalyticProcessorService, guildCount: Int): JsonObject {
		when (service) {
			DISCORD_BOT_LIST -> {
				return jsonObject(
						"server_count" to guildCount
				)
			}
			DISCORD_BOTS -> {
				return jsonObject(
						"guildCount" to guildCount
				)
			}
			else -> throw UnsupportedAnalyticServiceException()
		}
	}
}