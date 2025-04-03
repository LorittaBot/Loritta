package net.perfectdreams.loritta.morenitta.twitch

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class TwitchAPI(val clientId: String,
				val clientSecret: String,
				var accessToken: String? = null,
	// https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
	// var refreshToken: String? = null,
				var expiresIn: Long? = null,
				var generatedAt: Long? = null
) {
	companion object {
		private const val PREFIX = "https://id.twitch.tv"
		private const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
		private const val USER_AGENT = "Loritta-Morenitta-Twitch-Auth/1.0"
		private val gson = Gson()
		private val logger = KotlinLogging.logger {}
		val http = HttpClient {
			this.expectSuccess = false
		}
	}

	val cachedStreamerInfo = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(7500).build<String, StreamerInfo>().asMap()
	val cachedStreamerInfoById = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(7500).build<Long, StreamerInfo>().asMap()
	val cachedGames = Caffeine.newBuilder().expireAfterWrite(8, TimeUnit.HOURS).maximumSize(1000).build<String, GameInfo>().asMap()

	private val mutex = Mutex()

	suspend fun doTokenExchange(): JsonObject {
		logger.info { "doTokenExchange()" }

		val parameters = Parameters.build {
			append("client_id", clientId)
			append("client_secret", clientSecret)
			append("grant_type", "client_credentials")
		}

		return doStuff(checkForRefresh = false) {
			val result = http.post {
				url(TOKEN_BASE_URL)
				userAgent(USER_AGENT)

				setBody(
					TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
				)
			}.bodyAsText()

			logger.info { result }

			val tree = JsonParser.parseString(result).asJsonObject

			if (tree.has("error"))
				throw TokenExchangeException("Error while exchanging token: ${tree["error"].asString}")

			readTokenPayload(tree)

			tree
		}
	}

	suspend fun refreshToken() {
		logger.info { "refreshToken()" }
		// https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
		doTokenExchange()
	}

	private suspend fun refreshTokenIfNeeded() {
		logger.info { "refreshTokenIfNeeded()" }
		if (accessToken == null)
			throw NeedsRefreshException()

		val generatedAt = generatedAt
		val expiresIn = expiresIn

		if (generatedAt != null && expiresIn != null) {
			if (System.currentTimeMillis() >= generatedAt + (expiresIn * 1000))
				throw NeedsRefreshException()
		}

		return
	}

	private suspend fun <T> doStuff(checkForRefresh: Boolean = true, callback: suspend () -> (T)): T {
		logger.info { "doStuff(...) mutex locked? ${mutex.isLocked}" }
		return try {
			if (checkForRefresh)
				refreshTokenIfNeeded()

			mutex.withLock {
				callback.invoke()
			}
		} catch (e: RateLimitedException) {
			logger.info { "rate limited exception! locked? ${mutex.isLocked}" }
			return doStuff(checkForRefresh, callback)
		} catch (e: NeedsRefreshException) {
			logger.info { "refresh exception!" }
			refreshToken()
			doStuff(checkForRefresh, callback)
		}
	}

	private fun readTokenPayload(payload: JsonObject) {
		accessToken = payload["access_token"].string
		// https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
		// refreshToken = payload["refresh_token"].string
		expiresIn = payload["expires_in"].long
		generatedAt = System.currentTimeMillis()
	}

	private suspend fun checkForRateLimit(element: JsonElement): Boolean {
		if (element.isJsonObject) {
			val asObject = element.obj
			if (asObject.has("retry_after")) {
				val retryAfter = asObject["retry_after"].long

				logger.info { "Got rate limited, oof! Retry After: $retryAfter" }
				// oof, ratelimited!
				delay(retryAfter)
				throw RateLimitedException()
			}
		}

		return false
	}

	private suspend fun checkIfRequestWasValid(response: HttpResponse): HttpResponse {
		if (response.status.value == 401)
			throw TokenUnauthorizedException(response.status)

		return response
	}

	suspend fun getUserLogin(login: String): StreamerInfo? {
		val loginAsLowerCase = login.lowercase()
		return getUserLogins(listOf(loginAsLowerCase))[loginAsLowerCase]
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
			val payload = makeTwitchApiRequest(url).bodyAsText()
			logger.trace { payload }

			val response = JsonParser.parseString(payload).obj

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

	suspend fun getUserLoginById(login: Long): StreamerInfo? {
		return getUserLoginsById(listOf(login))[login]
	}

	suspend fun getUserLoginsById(userLogins: List<Long>): Map<Long, StreamerInfo> {
		// Vamos criar uma "lista" de IDs para serem procurados (batching)
		val results = mutableMapOf<Long, StreamerInfo>()

		val queryUserLogins = mutableSetOf<Long>()
		for (login in userLogins) {
			if (cachedStreamerInfoById.contains(login)) {
				results[login] = cachedStreamerInfoById[login]!!
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
					query += "?id=${URLEncoder.encode(it.toString(), "UTF-8")}"
				} else {
					query += "&id=${URLEncoder.encode(it.toString(), "UTF-8")}"
				}
			}

			val url = "https://api.twitch.tv/helix/users$query"
			val payload = makeTwitchApiRequest(url).bodyAsText()
			logger.trace { payload }

			val response = JsonParser.parseString(payload).obj

			try {
				val data = response["data"].array
				logger.debug { "queryUserLogins payload contém ${data.size()} objetos!" }

				data.forEach {
					val obj = gson.fromJson<StreamerInfo>(it)

					cachedStreamerInfo[obj.login] = obj
					results[obj.id] = obj
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
			.bodyAsText()

		val response = JsonParser.parseString(payload).obj

		val data = response["data"].array

		if (data.size() == 0) {
			return null
		}

		val channel = data[0].obj
		val gameInfo = LorittaBot.GSON.fromJson<GameInfo>(channel)
		cachedGames[gameId] = gameInfo

		return LorittaBot.GSON.fromJson(channel)
	}

	suspend fun makeTwitchApiRequest(url: String, method: String = "GET", form: Map<String, String>? = null): HttpResponse {
		return doStuff {
			val result = checkIfRequestWasValid(
				http.request(url) {
					if (method == "POST")
						this.method = HttpMethod.Post
					else
						this.method = HttpMethod.Get

					userAgent(Constants.USER_AGENT)
					header("Authorization", "Bearer $accessToken")
					header("Client-ID", clientId)

					if (form != null)
						setBody(
							FormDataContent(
								Parameters.build {
									for ((key, value) in form)
										this.append(key, value)
								}
							)
						)
				}
			)
			result
		}
	}

	class TokenUnauthorizedException(status: HttpStatusCode) : RuntimeException()
	class TokenExchangeException(message: String) : RuntimeException(message)
	private class RateLimitedException : RuntimeException()
	private class NeedsRefreshException : RuntimeException()

	data class GameInfo(
		@SerializedName("box_art_url")
		val boxArtUrl: String,
		val id: String,
		val name: String
	)

	data class StreamerInfo(
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
		val id: Long
	)
}