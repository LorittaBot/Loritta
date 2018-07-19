package com.mrpowergamerbr.loritta.livestreams

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.logger
import kotlinx.coroutines.experimental.delay
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TwitchUtils {
	val userLogin2Id = ConcurrentHashMap<String, String>()
	val logger by logger()

	fun queryUserLogins(userLogins: List<String>) {
		// Vamos criar uma "lista" de IDs para serem procurados (batching)
		val batchs = mutableListOf<ArrayList<String>>()

		var currentBatch = arrayListOf<String>()

		for (userLogin in userLogins) {
			if (userLogin2Id.contains(userLogin))
				continue

			if (currentBatch.size == 100) {
				batchs.add(currentBatch)
				currentBatch = arrayListOf<String>()
			}
			currentBatch.add(userLogin)
		}

		batchs.add(currentBatch)

		for (userLogins in batchs) {
			if (userLogins.isEmpty())
				continue

			logger.info("Pegando informações de usuários da Twitch: ${userLogins.joinToString(", ")}")
			var query = ""
			userLogins.forEach {
				if (query.isEmpty()) {
					query += "?login=${URLEncoder.encode(it.trim(), "UTF-8")}"
				} else {
					query += "&login=${URLEncoder.encode(it.trim(), "UTF-8")}"
				}
			}

			val url = "https://api.twitch.tv/helix/users$query"
			val payload = makeTwitchApiRequest(url).body()

			val response = jsonParser.parse(payload).obj

			try {
				val data = response["data"].array
				logger.info("queryUserLogins payload contém ${data.size()} objetos!")

				data.forEach {
					val obj = it.obj

					userLogin2Id[obj["login"].string] = obj["id"].string
				}
			} catch (e: IllegalStateException) {
				logger.error("Estado inválido ao manipular payload de queryUserLogins! ${payload}", e)
				throw e
			}
		}
	}

	fun makeTwitchApiRequest(url: String, method: String = "GET", form: Map<String, String>? = null): HttpRequest {
		val request = HttpRequest(url, method).userAgent(Constants.USER_AGENT).header("Client-ID", Loritta.config.twitchClientId)

		if (form != null)
			request.form(form)

		if (request.code() == 429) { // too many requests
			val resetsAt = (request.header("Ratelimit-Reset").toLong() * 1000) - System.currentTimeMillis()
			logger.info("Rate limit atingido! Nós iremos continuar daqui ${resetsAt}ms")
			Thread.sleep(resetsAt)
			return makeTwitchApiRequest(url, method, form)
		}

		return request
	}

	// Versão para ser utilizada com Kotlin Coroutines
	suspend fun makeTwitchApiRequestSuspend(url: String, method: String = "GET", form: Map<String, String>? = null): HttpRequest {
		val request = HttpRequest(url, method).userAgent(Constants.USER_AGENT).header("Client-ID", Loritta.config.twitchClientId)

		if (form != null)
			request.form(form)

		if (request.code() == 429) { // too many requests
			val resetsAt = (request.header("Ratelimit-Reset").toLong() * 1000) - System.currentTimeMillis()
			logger.info("Rate limit atingido! (suspend) Nós iremos continuar daqui ${resetsAt}ms")
			delay(resetsAt)
			return makeTwitchApiRequestSuspend(url, method, form)
		}

		return request
	}
}