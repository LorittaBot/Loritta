package com.mrpowergamerbr.loritta.livestreams

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.google.gson.annotations.SerializedName
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TwitchAPI {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val cachedStreamerInfo = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(7500).build<String, StreamerInfo>().asMap()
	val cachedGames = Caffeine.newBuilder().expireAfterWrite(8, TimeUnit.HOURS).maximumSize(1000).build<String, GameInfo>().asMap()

	val isRatelimited: Boolean
		get() = ratelimitResetsAt > System.currentTimeMillis()
	var ratelimitResetsAt = 0L

	suspend fun getUserLogin(login: String): StreamerInfo? {
		return getUserLogins(listOf(login))[login]
	}

	suspend fun getUserLogins(userLogins: List<String>): Map<String, StreamerInfo> {
		// Vamos criar uma "lista" de IDs para serem procurados (batching)
		val results = mutableMapOf<String, StreamerInfo>()

		val queryUserLogins = mutableSetOf<String>()
		for (login in userLogins) {
			if (cachedStreamerInfo.contains(login)) {
				results[login] = cachedStreamerInfo[login]!!
			} else {
				queryUserLogins.add(login)
			}
		}

		val batches = queryUserLogins.chunked(100)
		for (userLogins in batches) {
			if (userLogins.isEmpty())
				continue

			logger.debug { "Pegando informações de usuários da Twitch: ${userLogins.joinToString(", ")}" }
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
			logger.trace { payload }

			val response = jsonParser.parse(payload).obj

			try {
				val data = response["data"].array
				logger.debug { "queryUserLogins payload contém ${data.size()} objetos!" }

				data.forEach {
					val obj = gson.fromJson<StreamerInfo>(it)

					cachedStreamerInfo[obj.login] = obj
					results[obj.login] = obj
				}
			} catch (e: IllegalStateException) {
				logger.error(e) { "Estado inválido ao manipular payload de queryUserLogins! ${payload}" }
				throw e
			}
		}

		return results
	}

	suspend fun getGameInfo(gameId: String): GameInfo? {
		if (cachedGames.containsKey(gameId))
			return cachedGames[gameId]

		val payload = makeTwitchApiRequest("https://api.twitch.tv/helix/games?id=$gameId")
				.body()

		val response = jsonParser.parse(payload).obj

		val data = response["data"].array

		if (data.size() == 0) {
			return null
		}

		val channel = data[0].obj
		val gameInfo = Loritta.GSON.fromJson<GameInfo>(channel)
		cachedGames[gameId] = gameInfo

		return Loritta.GSON.fromJson(channel)
	}

	suspend fun makeTwitchApiRequest(url: String, method: String = "GET", form: Map<String, String>? = null): HttpRequest {
		if (isRatelimited) {
			val delay = ratelimitResetsAt - System.currentTimeMillis()
			delay(delay)
		}

		val request = HttpRequest(url, method).userAgent(Constants.USER_AGENT).header("Client-ID", loritta.config.twitch.clientId)

		if (form != null)
			request.form(form)

		if (request.code() == 429) { // too many requests
			ratelimitResetsAt = request.header("Ratelimit-Reset").toLong() * 1000
			waitUntilRatelimitIsLifted()
			return makeTwitchApiRequest(url, method, form)
		}

		return request
	}

	private suspend fun waitUntilRatelimitIsLifted() {
		val delay = ratelimitResetsAt - System.currentTimeMillis()
		logger.warn { "Rate limit atingido! Nós iremos continuar daqui ${delay}ms" }
		delay(delay)
	}

	class GameInfo(
			@SerializedName("box_art_url")
			val boxArtUrl: String,
			val id: String,
			val name: String
	)

	class StreamerInfo(
			@SerializedName("broadcaster_type")
			val broadcasterType: String,
			val description: String,
			@SerializedName("profile_image_url")
			val profileImageUrl: String,
			@SerializedName("offline_image_url")
			val offlineImageUrl: String,
			@SerializedName("view_count")
			val viewCount: Long,
			@SerializedName("display_name")
			val displayName: String,
			val login: String,
			val id: String
	)
}