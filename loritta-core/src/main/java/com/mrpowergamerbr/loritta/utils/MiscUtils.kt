package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import org.jooby.Status
import org.json.XML
import org.jsoup.Jsoup
import org.jsoup.UncheckedIOException
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

	fun getInviteId(url: String, count: Int = 0): String? {
		if (count == 5) // Stuck in a loop
			return null

		try {
			val temmie = TemmieBitly("R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs")
			var newUrl = url.removePrefix(".").removeSuffix(".")
			val bitlyUrl = temmie.expand(url)
			if (!bitlyUrl!!.contains("NOT_FOUND") && !bitlyUrl.contains("RATE_LIMIT_EXCEEDED")) {
				newUrl = bitlyUrl
			}
			val httpRequest = HttpRequest.get(newUrl)
					.doSafeConnection()
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

			// Se não encontrar nenhum invite, vamos tentar parsear a página para pegar http redirects no <head> da página
			// Caso seja uma imagem (ou algo que seja binário) o Jsoup retorna UncheckedIOException
			val parser = try { Jsoup.parse(httpRequest.body()) } catch (e: UncheckedIOException) { return null }
			val htmlRedirects = parser.getElementsByTag("meta").filter { it.hasAttr("http-equiv") && it.hasAttr("content") }

			htmlRedirects.forEach {
				val content = it.attr("content") // O formato é algo assim: 3;url=https://discordapp.com/invite/xyzabc

				val urlEqualsIndex = content.indexOf("url=")

				if (urlEqualsIndex == -1) // Caso não tenha um "url=", vamos ignorar
					return@forEach

				val redirectUrl = content.substring(urlEqualsIndex + "url=".length)

				val externalInviteId = getInviteId(redirectUrl, count + 1)
				if (externalInviteId != null)
					return externalInviteId
			}

			return null
		} catch (e: HttpRequest.HttpRequestException) {
			return null
		}
	}

	/**
	 * Strips all links from the [string]
	 */
	fun stripLinks(string: String): String {
		var output = string
		val matcher = Constants.URL_PATTERN.matcher(
				string.replace("\u200B", "")
						.replace("\\", "")
		)

		while (matcher.find()) {
			val url = matcher.group()
			output = string.replace(url, "")
		}
		return output
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

		logger.info("Verifying IP: $ip")
		// Antes de nós realmente decidir "ele deu upvote então vamos dar o upvote", nós iremos verificar o IP no StopForumSpam
		val stopForumSpam = HttpRequest.get("http://api.stopforumspam.org/api?ip=$ip")
				.body()

		logger.info("Stop Forum Spam: $stopForumSpam")

		// STOP FORUM SPAM
		val xmlJSONObj = XML.toJSONObject(stopForumSpam)

		logger.info("as JSON: $xmlJSONObj")

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

	fun handleVerification(status: AccountCheckResult) {
		if (!status.canAccess) {
			when (status) {
				MiscUtils.AccountCheckResult.STOP_FORUM_SPAM,
				MiscUtils.AccountCheckResult.BAD_HOSTNAME,
				MiscUtils.AccountCheckResult.OVH_HOSTNAME -> {
					// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
					// 0 = Stop Forum Spam
					// 1 = Bad hostname
					// 2 = OVH IP
					throw WebsiteAPIException(Status.FORBIDDEN,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN,
									"Bad IP!"
							) {
								it["code"] = 3
								it["type"] = when (status) {
									MiscUtils.AccountCheckResult.STOP_FORUM_SPAM -> 0
									MiscUtils.AccountCheckResult.BAD_HOSTNAME -> 1
									MiscUtils.AccountCheckResult.OVH_HOSTNAME -> 2
									else -> -1
								}
							}
					)
				}
				MiscUtils.AccountCheckResult.BAD_EMAIL -> {
					throw WebsiteAPIException(Status.FORBIDDEN,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN,
									"Bad email!"
							) { it["code"] = 2 }
					)
				}
				MiscUtils.AccountCheckResult.NOT_VERIFIED -> {
					throw WebsiteAPIException(Status.FORBIDDEN,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN,
									"Account is not verified!"
							) { it["code"] = 1 }
					)
				}
				else -> throw WebsiteAPIException(Status.SERVER_ERROR, jsonObject("reason" to "Missing !canAccess result! ${status.name}"))
			}
		}
	}

	fun checkRecaptcha(serverToken: String, clientToken: String): Boolean {
		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${serverToken}&response=$clientToken")
				.body()

		val jsonParser = jsonParser.parse(body).obj
		return jsonParser["success"].bool
	}

	fun hasInappropriateWords(string: String): Boolean {
		val lowerCaseNickname = string.toLowerCase()
				.replace("4", "a")
				.replace("@", "a")
				.replace("1", "i")
				.replace("0", "o")

		return Constants.BAD_NICKNAME_WORDS.any {
			lowerCaseNickname.contains(it)
		}
	}

	fun hasInvite(string: String, whitelistedInvites: List<String> = listOf()): Boolean {
		val matcher = Constants.URL_PATTERN.matcher(string)

		while (matcher.find()) {
			var url = matcher.group()
			if (url.contains("discord") && url.contains("gg")) {
				url = "discord.gg" + matcher.group(1).replace(".", "")
			}

			val inviteId = MiscUtils.getInviteId("http://$url") ?: MiscUtils.getInviteId("https://$url")

			if (inviteId != null) { // INVITES DO DISCORD
				if (inviteId != "attachments" && inviteId != "forums" && !whitelistedInvites.contains(inviteId))
					return true // Tem convites válidos?
			}
		}

		return false
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