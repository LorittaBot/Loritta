package net.perfectdreams.temmiediscordauth

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
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
	val rateLimitCheckMutex: Mutex,
) {
	companion object {
		private const val PREFIX = "https://discordapp.com/api"
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

			if (tree.has("error"))
				throw TokenExchangeException("Error while exchanging token: ${tree["error"].asString}")

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
			val result = checkIfRequestWasValid(
				http.post {
					url(TOKEN_BASE_URL)
					userAgent(USER_AGENT)

					setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
				}
			)

			logger.info { result }

			checkForRateLimit(result)

			val resultAsJson = JsonParser.parseString(result.bodyAsText()).asJsonObject

			if (resultAsJson.isJsonObject && resultAsJson.has("error"))
				throw TokenExchangeException("Error while exchanging token: ${resultAsJson["error"].asString}")

			readTokenPayload(resultAsJson.obj)
		}
	}

	suspend fun getUserIdentification(): UserIdentification {
		logger.info { "getUserIdentification()" }
		return doStuff {
			val result = checkIfRequestWasValid(
				http.get {
					url(USER_IDENTIFICATION_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			logger.info { result }

			val resultAsJson = JsonParser.parseString(result.bodyAsText())
			checkForRateLimit(result)

			return@doStuff gson.fromJson<UserIdentification>(resultAsJson)
		}
	}

	suspend fun getUserGuilds(): List<Guild> {
		logger.info { "getUserGuilds()" }
		return doStuff {
			val result = checkIfRequestWasValid(
				http.get {
					url(USER_GUILDS_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			logger.info { result }

			val resultAsJson = JsonParser.parseString(result.bodyAsText())
			checkForRateLimit(result)

			return@doStuff gson.fromJson<List<Guild>>(resultAsJson)
		}
	}

	suspend fun getUserConnections(): List<Connection> {
		logger.info { "getUserConnections()" }
		return doStuff {
			val result = checkIfRequestWasValid(
				http.get {
					url(CONNECTIONS_URL)
					userAgent(USER_AGENT)
					header("Authorization", "Bearer $accessToken")
				}
			)

			logger.info { result }

			val resultAsJson = JsonParser.parseString(result.bodyAsText())
			checkForRateLimit(result)

			return@doStuff gson.fromJson<List<Connection>>(resultAsJson)
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
		logger.info { "doStuff(...) Is mutex locked for rate limiting? ${rateLimitCheckMutex.isLocked}" }
		// Lock for rate limit checks before proceeding
		rateLimitCheckMutex.withLock {}

		return try {
			if (checkForRefresh)
				refreshTokenIfNeeded()

			callback.invoke()
		} catch (e: RateLimitedException) {
			logger.info { "rate limited exception! locked? ${rateLimitCheckMutex.isLocked}" }
			return doStuff(checkForRefresh, callback)
		} catch (e: NeedsRefreshException) {
			logger.info { "refresh exception!" }
			refreshToken()
			doStuff(checkForRefresh, callback)
		}
	}

	private fun readTokenPayload(payload: JsonObject) {
		accessToken = payload["access_token"].string
		refreshToken = payload["refresh_token"].string
		expiresIn = payload["expires_in"].long
		generatedAt = System.currentTimeMillis()
	}

	private suspend fun checkForRateLimit(response: HttpResponse): Boolean {
		val header = response.headers["X-RateLimit-Reset-After"]
		if (header != null) {
			rateLimitCheckMutex.withLock {
				val retryAfterTime = header.toDouble().seconds
				logger.info { "Got rate limited, oof! Retry After: $retryAfterTime" }
				// oof, ratelimited!
				delay(retryAfterTime)
				throw RateLimitedException()
			}
		}

		return false
	}

	private fun checkIfRequestWasValid(response: HttpResponse): HttpResponse {
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
		val permissions: Int
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