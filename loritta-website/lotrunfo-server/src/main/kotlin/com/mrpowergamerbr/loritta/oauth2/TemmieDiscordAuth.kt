package com.mrpowergamerbr.loritta.oauth2

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import mu.KotlinLogging
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class TemmieDiscordAuth {
	companion object {
		const val PREFIX = "https://discordapp.com/api"
		const val USER_IDENTIFICATION_URL = "$PREFIX/users/@me"
		const val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"
		const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
		const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0"
		val gson = Gson()
		val jsonParser = JsonParser()
		private val logger = KotlinLogging.logger {}
	}

	private var authCode: String
	private var redirectUri: String
	private var clientId: String
	private var clientSecret: String
	var debug = false

	var accessToken: String? = null
	private var refreshToken: String? = null
	private var expiresIn: Long? = null
	private var generatedIn: Long? = null

	constructor(authCode: String, redirectUri: String, clientId: String, clientSecret: String) {
		this.authCode = authCode
		this.redirectUri = redirectUri
		this.clientId = clientId
		this.clientSecret = clientSecret
	}

	fun isReady(ignoreRefresh: Boolean = false) {
		if (accessToken == null)
			throw NotLoggedInException()

		if (!ignoreRefresh && !isValid()) {
			refreshToken()
		}
	}

	fun doTokenExchange() {
		val variables = mapOf(
			"code" to authCode,
			"grant_type" to "authorization_code",
			"redirect_uri" to redirectUri,
			"client_id" to clientId,
			"client_secret" to clientSecret
		)
		doTokenExchange(variables)
	}

	fun refreshToken() {
		val variables = mapOf(
				"refresh_token" to refreshToken!!,
				"grant_type" to "refresh_token",
				"client_id" to clientId,
				"client_secret" to clientSecret,
				"redirect_uri" to redirectUri,
				"scope" to "identify guilds email guilds.join"
		)

		val response = HttpRequest.post(TOKEN_BASE_URL)
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.send(buildQuery(variables))

		val body = response.body()
		_println(body)

		val json = TemmieDiscordAuth.jsonParser.parse(body).obj

		if (json.has("error")) {
			logger.error("Erro ao tentar fazer refresh no token para ${clientId}: $body")
			throw TokenExchangeException()
		}

		readTokenPayload(json)
	}

	private fun doTokenExchange(variables: Map<String, String>) {
		_println(buildQuery(variables))
		val response = HttpRequest.post(TOKEN_BASE_URL)
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.send(buildQuery(variables))

		val body = response.body()
		_println(body)

		val json = TemmieDiscordAuth.jsonParser.parse(body).obj

		if (json.has("error")) {
			logger.error("Erro ao tentar fazer token exchange para ${clientId}: $body")
			throw TokenExchangeException()
		}

		readTokenPayload(json)
	}

	private fun readTokenPayload(json: JsonObject) {
		this.accessToken = json["access_token"].string
		this.refreshToken = json["refresh_token"].string
		this.expiresIn = json["expires_in"].long
		this.generatedIn = System.currentTimeMillis()
	}

	private fun checkForRateLimit(element: JsonElement): Boolean {
		if (!element.isJsonObject)
			return false

		val obj = element.obj

		if (!obj.has("retry_after"))
			return false

		Thread.sleep(obj["retry_after"].long)
		return true
	}

	fun getUserIdentification(): UserIdentification {
		isReady()
		val response = HttpRequest.get(USER_IDENTIFICATION_URL)
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.authorization("Bearer $accessToken")

		checkStatusCode(response)
		val body = response.body()

		if (checkForRateLimit(jsonParser.parse(body)))
			return getUserIdentification()

		return gson.fromJson(body)
	}

	fun getUserGuilds(): List<DiscordGuild> {
		isReady()

		val response = HttpRequest.get(USER_GUILDS_URL)
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.authorization("Bearer $accessToken")

		checkStatusCode(response)

		val body = response.body()

		if (checkForRateLimit(jsonParser.parse(body)))
			return getUserGuilds()

		_println(body)
		return gson.fromJson(body)
	}

	private fun checkStatusCode(request: HttpRequest) {
		when (request.code()) {
			401 -> throw UnauthorizedException()
			405 -> throw MethodNotAllowedException()
		}
	}

	fun isValid(): Boolean {
		return (this.generatedIn!! + (this.expiresIn!! * 1000)) > System.currentTimeMillis()
	}

	private fun buildQuery(params: Map<String, Any>): String {
		val query = arrayOfNulls<String>(params.size)
		for ((index, key) in params.keys.withIndex()) {
			var value = (if (params[key] != null) params[key] else "").toString()
			try {
				value = URLEncoder.encode(value, "UTF-8")
			} catch (e: UnsupportedEncodingException) {
			}

			query[index] = "$key=$value"
		}

		return query.joinToString("&")
	}

	fun _println(obj: Any?) {
		if (debug) {
			println(obj.toString())
		}
	}

	class TokenExchangeException : RuntimeException()
	class NotLoggedInException : RuntimeException()
	class UnauthorizedException : RuntimeException()
	class MethodNotAllowedException : RuntimeException()

	class UserIdentification(
		username: String,
		val verified: Boolean,
		@SerializedName("mfa_enabled")
		val mfaEnabled: Boolean,
		id: String,
		avatar: String,
		discriminator: String,
		val email: String?,
		val bot: Boolean,
		val locale: String,
		val flags: Int,
		val premiumType: Int
	) : SimpleUserIdentification(username, id, avatar, discriminator)

	class DiscordGuild(
		val owner: Boolean,
		val permissions: Int,
		val icon: String,
		val id: String,
		val name: String
	)
}