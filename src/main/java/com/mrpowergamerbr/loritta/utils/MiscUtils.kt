package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly
import org.json.XML
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object MiscUtils {
	val logger = LoggerFactory.getLogger(MiscUtils::class.java)

	fun getResponseError(json: JsonObject): String? {
		if (!json.has("error"))
			return null

		return json["error"]["errors"][0]["reason"].string
	}

	fun isInvite(url: String): Boolean {
		return getInviteId(url) != null
	}

	fun getInviteId(url: String): String? {
		try {
			val temmie = TemmieBitly("R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs")
			var newUrl = url.removePrefix(".").removeSuffix(".")
			val bitlyUrl = temmie.expand(url)
			if (!bitlyUrl!!.contains("NOT_FOUND")) {
				newUrl = bitlyUrl!!
			}
			val httpRequest = HttpRequest.get(newUrl)
					.followRedirects(true)
					.connectTimeout(2500)
					.readTimeout(2500)
					.userAgent(Constants.USER_AGENT)
			httpRequest.ok()
			val location = httpRequest.headers().entries.firstOrNull { it.key == "Location" }?.value?.getOrNull(0)
			val url = location ?: httpRequest.url().toString()
			val matcher = Pattern.compile(".*(discord\\.gg|discordapp.com(/invite))/([A-z0-9]+).*").matcher(url)
			if (matcher.find()) {
				return matcher.group(3)
			}
			return null
		} catch (e: HttpRequest.HttpRequestException) {
			return null
		}
	}

	fun isJSONValid(jsonInString: String): Boolean {
		try {
			GSON.fromJson(jsonInString, Any::class.java)
			return true
		} catch (ex: com.google.gson.JsonSyntaxException) {
			return false
		}
	}

	fun optimizeGIF(file: File, lossy: Int = 200) {
		val processBuilder = ProcessBuilder(
				File(Loritta.FOLDER, "gifsicle-static").toString(), // https://github.com/kornelski/giflossy/releases
				"-i",
				file.toString(),
				"-O3",
				"--lossy=$lossy",
				"--colors",
				"256",
				"-o",
				file.toString())

		val process = processBuilder.start()
		process.waitFor(10, TimeUnit.SECONDS)
	}

	fun verifyAccount(userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): AccountCheckResult {
		if (!userIdentification.verified)
			return AccountCheckResult.NOT_VERIFIED

		val email = userIdentification.email ?: return AccountCheckResult.NOT_VERIFIED // Sem email == não verificado (?)

		val domain = email.split("@")
		if (2 > domain.size) // na verdade seria "INVALID_EMAIL" mas...
			return AccountCheckResult.NOT_VERIFIED

		val list = HttpRequest.get("https://raw.githubusercontent.com/martenson/disposable-email-domains/master/disposable_email_blacklist.conf")
				.body()
				.split("\n")
				.toMutableList()

		// Alguns emails que não estão na lista
		list.add("sparklmail.com")
		list.add("l8oaypr.com")

		// mailto.space
		try {
			val body = HttpRequest.get("https://mailto.space/get/inbox/c785304469fbf265b6c71965f194e653e4c4951c/wbydvhbby")
					.userAgent(Constants.USER_AGENT)
					.body()

			val element = jsonParser.parse(body)

			val array = element.array
			val domainsArray = array[1].array

			list.addAll(domainsArray.map { it.string })
		} catch (e: Exception) {
			logger.error("Erro ao tentar pegar email atual do mailto.space!", e)
		}

		val matches = list.any { it == domain[1] }

		if (matches)
			return AccountCheckResult.BAD_EMAIL

		return verifyIP(ip)
	}

	fun verifyIP(ip: String): AccountCheckResult {
		// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
		// 0 = Stop Forum Spam
		// 1 = Bad hostname
		// 2 = OVH IP

		// Antes de nós realmente decidir "ele deu upvote então vamos dar o upvote", nós iremos verificar o IP no StopForumSpam
		val stopForumSpam = HttpRequest.get("http://api.stopforumspam.org/api?ip=$ip")
				.body()

		// STOP FORUM SPAM
		val xmlJSONObj = XML.toJSONObject(stopForumSpam)

		val response = jsonParser.parse(xmlJSONObj.toString(4)).obj["response"]

		val isSpam = response["appears"].bool

		if (isSpam)
			return AccountCheckResult.STOP_FORUM_SPAM

		// HOSTNAME BLOCC:tm:
		val addr = InetAddress.getByName(ip)
		val host = addr.hostName.toLowerCase()

		val hostnames = listOf(
				"anchorfree", // Hotspot Shield
				"ipredator.se", // IP redator
				"pixelfucker.org", // Pixelfucker
				"theremailer.net", // TheRemailer
				"tor-exit", // Tor Exit
				"torexit",
				"exitpoint"
		)

		val badHostname = hostnames.any { host.contains(it) }

		if (badHostname)
			return AccountCheckResult.BAD_HOSTNAME

		// OVH BLOCC:tm:
		if (host.matches(Regex(".*ns[0-9]+.*")))
			return AccountCheckResult.OVH_HOSTNAME

		return AccountCheckResult.SUCCESS
	}

	enum class AccountCheckResult(val canAccess: Boolean) {
		SUCCESS(true),
		NOT_VERIFIED(false),
		BAD_EMAIL(false),
		STOP_FORUM_SPAM(false),
		BAD_HOSTNAME(false),
		OVH_HOSTNAME(false)
	}
}