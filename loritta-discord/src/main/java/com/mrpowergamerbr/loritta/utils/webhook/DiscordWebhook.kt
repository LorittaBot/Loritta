package com.mrpowergamerbr.loritta.utils.webhook

import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordWebhook(
		val url: String,
		val httpClient: HttpClient,
		val coroutineDispatcher: CoroutineDispatcher
) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var requestQueue = mutableListOf<Pair<DiscordMessage, Continuation<JsonObject>>>()
	var isRateLimited = false

	suspend fun send(message: DiscordMessage, wait: Boolean = false): JsonObject {
		if (isRateLimited) {
			logger.trace { "Message $message to $url is rate limited! Adding to requestQueue..." }
			return suspendCoroutine {
				requestQueue.add(Pair(message, it))
			}
		}

		logger.trace { "Sending $message to $url" }

		val result = GlobalScope.async(coroutineDispatcher) {
			var url = url
			if (wait) {
				url += "?wait=true"
			}

			val response = httpClient.post<String>(url) {
				header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				body = TextContent(GSON.toJson(message), ContentType.Application.Json)
			}

			if (response.isNotEmpty()) { // oh no
				val json = jsonParser.parse(response).obj

				if (json.contains("retry_after")) { // Rate limited, vamos colocar o request dentro de uma queue
					isRateLimited = true

					logger.debug { "Request $message to $url got rate limited! Retry after: ${json["retry_after"].long}"}

					delay(json["retry_after"].long)

					isRateLimited = false

					val requests = requestQueue.toMutableList()
					requestQueue.clear()

					for (queue in requests) {
						queue.second.resume(send(queue.first, wait))
					}

					return@async send(message, wait)
				} else {
					return@async json
				}
			} else { throw IllegalArgumentException("When sending $message to $url, the payload was empty!") }
		}

		return result.await()
	}
}