package net.perfectdreams.temmiediscordauth

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.content.TextContent
import io.ktor.http.formUrlEncode
import io.ktor.http.userAgent
import mu.KotlinLogging

class TemmieDiscordAuth(val clientId: String,
						val clientSecret: String,
						val authCode: String?,
						val redirectUri: String,
						val scope: List<String>,
						var accessToken: String? = null,
						var refreshToken: String? = null,
						var expiresIn: Long? = null,
						var generatedAt: Long? = null
) {
	companion object {
		private const val PREFIX = "https://discordapp.com/api"
		private const val USER_IDENTIFICATION_URL = "$PREFIX/users/@me"
		private const val CONNECTIONS_URL = "$USER_IDENTIFICATION_URL/connections"
		private const val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"
		private const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
		private const val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
		private val mapper = ObjectMapper()
		private val logger = KotlinLogging.logger {}
	}

	val http = HttpClient {
		this.expectSuccess = false
	}

	suspend fun doTokenExchange() {
		val authCode = authCode ?: throw RuntimeException("Trying to do token exchange without authCode!")

		val parameters = Parameters.build {
			append("client_id", clientId)
			append("client_secret", clientSecret)
			append("grant_type", "authorization_code")
			append("code", authCode)
			append("redirect_uri", redirectUri)
			append("scope", scope.joinToString(" "))
		}

		val result = http.post<String> {
			url(TOKEN_BASE_URL)
			userAgent(USER_AGENT)

			body = TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
		}

		logger.debug { result }

		val tree = mapper.readTree(result)

		if (tree["error"]?.textValue() != null)
			throw TokenExchangeException("Error while exchanging token: ${tree["error"].textValue()}")

		readTokenPayload(tree)
	}

	suspend fun refreshToken() {
		val refreshToken = refreshToken ?: throw RuntimeException()

		val parameters = Parameters.build {
			append("client_id", clientId)
			append("client_secret", clientSecret)
			append("grant_type", "refresh_token")
			append("refresh_token", refreshToken)
			append("redirect_uri", redirectUri)
			append("scope", scope.joinToString(" "))
		}

		val result = http.post<String> {
			url(TOKEN_BASE_URL)
			userAgent(USER_AGENT)

			body = TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
		}

		logger.debug { result }

		val tree = mapper.readTree(result)

		if (tree["error"]?.textValue() != null)
			throw TokenExchangeException("Error while exchanging token: ${tree["error"].textValue()}")

		readTokenPayload(tree)
	}

	suspend fun getUserIdentification(): UserIdentification {
		val result = http.get<String> {
			url(USER_IDENTIFICATION_URL)
			userAgent(USER_AGENT)
			header("Authorization", "Bearer $accessToken")
		}

		logger.debug { result }

		return mapper.readValue(result)
	}

	suspend fun getUserGuilds(): List<Guild> {
		val result = http.get<String> {
			url(USER_GUILDS_URL)
			userAgent(USER_AGENT)
			header("Authorization", "Bearer $accessToken")
		}

		logger.debug { result }

		return mapper.readValue(result)
	}

	suspend fun getUserConnections(): List<Connection> {
		val result = http.get<String> {
			url(CONNECTIONS_URL)
			userAgent(USER_AGENT)
			header("Authorization", "Bearer $accessToken")
		}

		logger.debug { result }

		return mapper.readValue(result)
	}

	private fun readTokenPayload(payload: JsonNode) {
		accessToken = payload["access_token"].textValue()
		refreshToken = payload["refresh_token"].textValue()
		expiresIn = payload["expires_in"].longValue()
		generatedAt = System.currentTimeMillis()
	}

	class TokenExchangeException(message: String) : RuntimeException(message)

	@JsonIgnoreProperties(ignoreUnknown = true)
	class UserIdentification @JsonCreator constructor(
			@JsonProperty("id")
			val id: String,
			@JsonProperty("username")
			val username: String,
			@JsonProperty("discriminator")
			val discriminator: String,
			@JsonProperty("avatar")
			val avatar: String?,
			@JsonProperty("bot")
			val bot: Boolean?,
			@JsonProperty("mfa_enabled")
			val mfaEnabled: Boolean?,
			@JsonProperty("locale")
			val locale: String?,
			@JsonProperty("verified")
			val verified: Boolean,
			@JsonProperty("email")
			val email: String?,
			@JsonProperty("flags")
			val flags: Int?,
			@JsonProperty("premium_type")
			val premiumType: Int?
	)

	@JsonIgnoreProperties(ignoreUnknown = true)
	class Guild @JsonCreator constructor(
			@JsonProperty("id")
			val id: String,
			@JsonProperty("name")
			val name: String,
			@JsonProperty("icon")
			val icon: String?,
			@JsonProperty("owner")
			val owner: Boolean,
			@JsonProperty("permissions")
			val permissions: Int
	)

	@JsonIgnoreProperties(ignoreUnknown = true)
	class Connection @JsonCreator constructor(
			@JsonProperty("id")
			val id: String,
			@JsonProperty("name")
			val name: String,
			@JsonProperty("type")
			val type: String,
			@JsonProperty("verified")
			val verified: Boolean,
			@JsonProperty("friend_sync")
			val friendSync: Boolean,
			@JsonProperty("show_activity")
			val showActivity: Boolean,
			@JsonProperty("visibility")
			val visibility: Int
	)
}