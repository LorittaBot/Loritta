package com.mrpowergamerbr.loritta.utils.oauth2

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class TemmiePatreonAuth {
	companion object {
		const val PREFIX = "https://api.patreon.com/oauth2/api"
		const val TOKEN_BASE_URL = "https://api.patreon.com/oauth2/token"
		const val PROJECT_PLEDGES_URL = "$PREFIX/campaigns/%s/pledges";
		const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0"
		val gson = Gson()
		val jsonParser = JsonParser()
	}

	var debug = false

	private var clientId: String
	private var clientSecret: String
	private var accessToken: String
	private var refreshToken: String
	private var expiresIn: Long
	private var generatedIn: Long? = null

	constructor(clientId: String, clientSecret: String, accessToken: String, refreshToken: String, expiresIn: Long) {
		this.clientId = clientId
		this.clientSecret = clientSecret
		this.accessToken = accessToken
		this.refreshToken = refreshToken
		this.expiresIn = expiresIn
	}

	fun isReady(ignoreRefresh: Boolean = false) {
		if (accessToken == null)
			throw NotLoggedInException()

		// if (!ignoreRefresh && !isValid()) {
			// refreshToken()
		//}
	}

	fun refreshToken() {
		isReady(true)

		val variables = mapOf(
				"refresh_token" to refreshToken!!,
				"grant_type" to "refresh_token",
				"client_id" to clientId!!,
				"client_secret" to clientSecret
		)

		val response = HttpRequest.post(TOKEN_BASE_URL)
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.send(buildQuery(variables))

		val body = response.body()
		_println(body)

		val json = TemmieDiscordAuth.jsonParser.parse(body).obj

		if (json.has("error"))
			throw TokenExchangeException()

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

		if (json.has("error"))
			throw TokenExchangeException()

		readTokenPayload(json)
	}

	private fun readTokenPayload(json: JsonObject) {
		this.accessToken = json["access_token"].string
		this.refreshToken = json["refresh_token"].string
		this.expiresIn = json["expires_in"].long
		this.generatedIn = System.currentTimeMillis()
	}

	fun getProjectPledges(projectId: String): MutableList<PatreonPledge> {
		isReady()
		val response = HttpRequest.get("https://www.patreon.com/api/oauth2/api/campaigns/$projectId/pledges?include=patron.null&${"page[count]=25".encodeToUrl()}")
				.header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.authorization("Bearer $accessToken")

		try {
			checkStatusCode(response)
		} catch (e: UnauthorizedException) {
			refreshToken()
			return getProjectPledges(projectId)
		}
		val body = response.body()

		var jsonObject = jsonParser.parse(body).obj

		val included = mutableMapOf<Int, JsonObject>()
		val patrons = mutableListOf<PatreonPledge>()

		fun addStuff(jsonObject: JsonObject) {
			for (element in jsonObject["included"].array) {
				val obj = element.obj
				val id = obj["id"].int
				included[id] = obj
			}

			for (element in jsonObject["data"].array) {
				if (element["attributes"].obj.has("amount_cents")) {
					val metadata = included[element["relationships"]["patron"]["data"]["id"].int]!!["attributes"].obj

					val fullName = metadata["full_name"].string
					var discordId: String? = null;

					if (metadata.has("social_connections")) {
						if (metadata["social_connections"].obj.has("discord")) {
							if (!metadata["social_connections"]["discord"].isJsonNull) {
								discordId = metadata["social_connections"]["discord"]["user_id"].string
							}
						}
					}

					patrons.add(PatreonPledge(fullName, element["attributes"].obj["amount_cents"].int, discordId, !element["attributes"]["declined_since"].isJsonNull))
				}
			}
		}

		addStuff(jsonObject)
		while (jsonObject["links"].obj["next"].nullString != null) {
			val response = HttpRequest.get(jsonObject["links"]["next"].string)
					.header("User-Agent", USER_AGENT)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.authorization("Bearer $accessToken")

			try {
				checkStatusCode(response)
			} catch (e: UnauthorizedException) {
				refreshToken()
				continue
			}

			val body = response.body()

			jsonObject = jsonParser.parse(body).obj

			addStuff(jsonObject)

		}

		return patrons
	}

	private fun checkStatusCode(request: HttpRequest) {
		when (request.code()) {
			401 -> throw UnauthorizedException()
			405 -> throw MethodNotAllowedException()
		}
	}

	fun isValid(): Boolean {
		return System.currentTimeMillis() > this.generatedIn!! + this.expiresIn!! * 1000
	}

	private fun buildQuery(params: Map<String, Any>): String {
		val query = arrayOfNulls<String>(params.size)
		for ((index, key) in params.keys.withIndex()) {
			var value = (if (params[key] != null) params[key] else "").toString()
			try {
				value = URLEncoder.encode(value, "UTF-8")
			} catch (e: UnsupportedEncodingException) {
			}

			query[index] = key + "=" + value
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

	class PatreonPledge(val fullName: String, val pledge: Int, val discordId: String?, val isDeclined: Boolean)
}