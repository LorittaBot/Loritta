package net.perfectdreams.temmiediscordauth

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class TemmieDiscordAuth(
	val clientId: String,
	val clientSecret: String,
	val authCode: String?,
	val redirectUri: String,
	val scope: List<String>,
	var accessToken: String? = null,
	var refreshToken: String? = null,
	var expiresIn: Long? = null,
	var generatedAt: Long? = null,
	/**
	 * A block to be invoked after the authentication data changes during a [readTokenPayload], useful to store the changed data in a session
	 */
	private val onTokenChange: (TemmieDiscordAuth) -> (Unit) = {}
) {
	companion object {
		private const val PREFIX = "https://discord.com/api/v10"
		private const val USER_IDENTIFICATION_URL = "$PREFIX/users/@me"
		private const val CONNECTIONS_URL = "$USER_IDENTIFICATION_URL/connections"
		private const val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"
		private const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
		private const val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
		private val gson = Gson()
		private val logger = KotlinLogging.logger {}
		val http = HttpClient {
			this.expectSuccess = false
		}

		val unknownTokenMutexKey = "unknown_token".hashCode()

		// TODO: This is *very* hacky and bad, and leaks int/mutex instances
		/**
		 * Stores mutexes per access token, each endpoint is rate limited based on the access token, and because multiple TemmieDiscordAuth instances can
		 * exist with the same token, we need to store it on a globally accessible map.
		 */
		val accessTokenMutexes = ConcurrentHashMap<Int, Mutex>()
	}

	suspend fun doTokenExchange(): JsonObject {
		logger.info { "doTokenExchange()" }
		val authCode = authCode ?: throw RuntimeException("Trying to do token exchange without authCode!")

		val parameters = Parameters.build {
			append("client_id", clientId)
			append("client_secret", clientSecret)
			append("grant_type", "authorization_code")
			append("code", authCode)
			append("redirect_uri", redirectUri)
			// append("scope", scope.joinToString(" "))
		}

		return doStuff {
			val result = http.post {
				url(TOKEN_BASE_URL)
				userAgent(USER_AGENT)

				setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
			}.bodyAsText()

			logger.info { result }

			val tree = JsonParser.parseString(result).asJsonObject

			// Discord seems to use the "errors" array for some things (like when your redirect uri is wrong) and the "error" string for other things (like when it is a invalid grant error)
			if (tree.has("errors"))
				throw TokenExchangeException("Error while exchanging token: ${tree["errors"].asJsonObject}")

			if (tree.has("error"))
				throw TokenExchangeException("Error while exchanging token: ${tree["error"].string}")

			readTokenPayload(tree)

			tree
		}
	}

	suspend fun refreshToken() {
		logger.info { "refreshToken()" }
		val refreshToken = refreshToken ?: throw RuntimeException()

		val parameters = Parameters.build {
			append("client_id", clientId)
			append("client_secret", clientSecret)
			append("grant_type", "refresh_token")
			append("refresh_token", refreshToken)
			append("redirect_uri", redirectUri)
			// append("scope", scope.joinToString(" "))
		}

		doStuff(false) {
			accessTokenMutexes.remove(accessToken ?: authCode ?: unknownTokenMutexKey)

			val httpResponse = checkIfRequestWasValid(
				http.post {
					url(TOKEN_BASE_URL)
					userAgent(USER_AGENT)

					setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
				}
			)

			println(httpResponse.status)
			val result = httpResponse.bodyAsText()
			logger.info { result }

			val tree = JsonParser.parseString(result).asJsonObject

			// Discord seems to use the "errors" array for some things (like when your redirect uri is wrong) and the "error" string for other things (like when it is a invalid grant error)
			if (tree.has("errors"))
				throw TokenExchangeException("Error while exchanging token: ${tree["errors"].asJsonObject}")

			if (tree.has("error"))
				throw TokenExchangeException("Error while exchanging token: ${tree["error"].string}")

			val resultAsJson = JsonParser.parseString(result)
			checkForRateLimit(httpResponse, resultAsJson)

			readTokenPayload(resultAsJson.obj)
		}
	}

	suspend fun getUserIdentification(): UserIdentification {
		logger.info { "getUserIdentification()" }
		return doStuff {
			val httpResponse = checkIfRequestWasValid(
				http.get {
					url(USER_IDENTIFICATION_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			val result = httpResponse.bodyAsText()
			logger.info { result }

			val resultAsJson = JsonParser.parseString(result)
			checkForRateLimit(httpResponse, resultAsJson)

			return@doStuff gson.fromJson<UserIdentification>(resultAsJson)
		}
	}

	suspend fun getUserGuilds(): List<Guild> {
		logger.info { "getUserGuilds()" }
		return doStuff {
			val httpResponse = checkIfRequestWasValid(
				http.get {
					url(USER_GUILDS_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			val result = httpResponse.bodyAsText()
			logger.info { result }

			val resultAsJson = JsonParser.parseString(result)
			checkForRateLimit(httpResponse, resultAsJson)

			return@doStuff gson.fromJson<List<Guild>>(result)
		}
	}

	suspend fun getUserConnections(): List<Connection> {
		logger.info { "getUserConnections()" }
		return doStuff {
			val httpResponse = checkIfRequestWasValid(
				http.get {
					url(CONNECTIONS_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			val result = httpResponse.bodyAsText()
			logger.info { result }

			val resultAsJson = JsonParser.parseString(result)
			checkForRateLimit(httpResponse, resultAsJson)

			return@doStuff gson.fromJson<List<Connection>>(result)
		}
	}

	private suspend fun refreshTokenIfNeeded() {
		logger.info { "refreshTokenIfNeeded()" }
		val generatedAt = generatedAt
		val expiresIn = expiresIn

		if (generatedAt != null && expiresIn != null) {
			if (System.currentTimeMillis() >= generatedAt + (expiresIn * 1000))
				throw NeedsRefreshException()
		}

		return
	}

	private suspend fun <T> doStuff(checkForRefresh: Boolean = true, callback: suspend () -> (T)): T {
		val mutexToken = accessToken ?: authCode ?: unknownTokenMutexKey
		val mutex = accessTokenMutexes.getOrPut(mutexToken.hashCode()) { Mutex() }

		logger.info { "doStuff(...) mutex locked? ${mutex.isLocked}; mutex token: $mutexToken" }
		return try {
			if (checkForRefresh)
				refreshTokenIfNeeded()

			mutex.withLock {
				callback.invoke()
			}
		} catch (e: RateLimitedException) {
			logger.info { "rate limited exception! locked? ${mutex.isLocked}; mutex token: $mutexToken" }
			return doStuff(checkForRefresh, callback)
		} catch (e: NeedsRefreshException) {
			logger.info { "refresh exception! locked? ${mutex.isLocked}; mutex token: $mutexToken" }
			refreshToken()
			doStuff(checkForRefresh, callback)
		}
	}

	private fun readTokenPayload(payload: JsonObject) {
		accessToken = payload["access_token"].string
		refreshToken = payload["refresh_token"].string
		expiresIn = payload["expires_in"].long
		generatedAt = System.currentTimeMillis()

		onTokenChange.invoke(this)
	}

	private suspend fun checkForRateLimit(response: HttpResponse, element: JsonElement): Boolean {
		if (element.isJsonObject) {
			val asObject = element.obj
			val retryAfterHeader = response.headers["Retry-After"]

			val retryAfter = if (retryAfterHeader != null) {
				retryAfterHeader.toLong().seconds
			} else if (asObject.has("retry_after")) {
				asObject["retry_after"].double.seconds
			} else null

			if (retryAfter != null) {
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

	class TokenUnauthorizedException(status: HttpStatusCode) : RuntimeException()
	class TokenExchangeException(message: String) : RuntimeException(message)

	class UserIdentification constructor(
		@SerializedName("id")
		val id: String,
		@SerializedName("username")
		val username: String,
		@SerializedName("discriminator")
		val discriminator: String,
		@SerializedName("avatar")
		val avatar: String?,
		@SerializedName("bot")
		val bot: Boolean?,
		@SerializedName("mfa_enabled")
		val mfaEnabled: Boolean?,
		@SerializedName("locale")
		val locale: String?,
		@SerializedName("verified")
		val verified: Boolean,
		@SerializedName("global_name")
		val globalName: String?,
		@SerializedName("email")
		val email: String?,
		@SerializedName("flags")
		val flags: Int?,
		@SerializedName("premium_type")
		val premiumType: Int?
	)

	class Guild constructor(
		@SerializedName("id")
		val id: String,
		@SerializedName("name")
		val name: String,
		@SerializedName("icon")
		val icon: String?,
		@SerializedName("owner")
		val owner: Boolean,
		@SerializedName("permissions")
		val permissions: Long,
		@SerializedName("features")
		val features: List<String>
	)

	class Connection constructor(
		@SerializedName("id")
		val id: String,
		@SerializedName("name")
		val name: String,
		@SerializedName("type")
		val type: String,
		@SerializedName("verified")
		val verified: Boolean,
		@SerializedName("friend_sync")
		val friendSync: Boolean,
		@SerializedName("show_activity")
		val showActivity: Boolean,
		@SerializedName("visibility")
		val visibility: Int
	)

	private class RateLimitedException : RuntimeException()
	private class NeedsRefreshException : RuntimeException()
}