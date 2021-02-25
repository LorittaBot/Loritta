package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.json.XML
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object MiscUtils {
	private val logger = KotlinLogging.logger {}

	fun getResponseError(json: JsonObject): String? {
		if (!json.has("error"))
			return null

		return json["error"]["errors"][0]["reason"].string
	}

	fun isInvite(url: String): Boolean {
		return getInviteId(url) != null
	}

	fun getInviteId(url: String): String? {
		val matcher = Constants.DISCORD_INVITE_PATTERN.matcher(url)
		if (matcher.find())
			return matcher.group(2)
		return null
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

	suspend fun verifyAccount(userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): AccountCheckResult {
		if (!userIdentification.verified)
			return AccountCheckResult.NOT_VERIFIED

		val email = userIdentification.email ?: return AccountCheckResult.NOT_VERIFIED // Sem email == não verificado (?)

		val domain = email.split("@")
		if (2 > domain.size) // na verdade seria "INVALID_EMAIL" mas...
			return AccountCheckResult.NOT_VERIFIED


		val list = File(Loritta.ASSETS, "data/blacklisted-emails.txt").readLines()

		val matches = list.any { it == domain[1] }

		if (matches)
			return AccountCheckResult.BAD_EMAIL

		return verifyIP(ip)
	}

	suspend fun verifyIP(ip: String): AccountCheckResult {
		// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
		// 0 = Stop Forum Spam
		// 1 = Bad hostname
		// 2 = OVH IP

		logger.info("Verifying IP: $ip")
		// Antes de nós realmente decidir "ele deu upvote então vamos dar o upvote", nós iremos verificar o IP no StopForumSpam
		val stopForumSpam = loritta.http.get<String>("http://api.stopforumspam.org/api?ip=$ip")

		logger.info("Stop Forum Spam: $stopForumSpam")

		// STOP FORUM SPAM
		val xmlJSONObj = XML.toJSONObject(stopForumSpam)

		logger.info("as JSON: $xmlJSONObj")

		val response = JsonParser.parseString(xmlJSONObj.toString(4)).obj["response"]

		val isSpam = response["appears"].nullBool

		if (isSpam == null) {
			logger.warn { "Appears response is missing from StopForumSpam response! Bug? We are going to ignore the spam check! Checked IP $ip and the response is $response" }
		} else {
			if (isSpam)
				return AccountCheckResult.STOP_FORUM_SPAM
		}

		// HOSTNAME BLOCC:tm:
		val host = withContext(Dispatchers.IO) {
			val addr = InetAddress.getByName(ip)
			// Don't be fooled: getHostName() is also blocking! That's why it needs to be within the Dispatchers.IO context ;)
			addr.hostName.toLowerCase()
		}

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
					throw WebsiteAPIException(HttpStatusCode.Forbidden,
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
					throw WebsiteAPIException(HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN,
									"Bad email!"
							) { it["code"] = 2 }
					)
				}
				MiscUtils.AccountCheckResult.NOT_VERIFIED -> {
					throw WebsiteAPIException(HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN,
									"Account is not verified!"
							) { it["code"] = 1 }
					)
				}
				else -> throw WebsiteAPIException(HttpStatusCode.InternalServerError, jsonObject("reason" to "Missing !canAccess result! ${status.name}"))
			}
		}
	}

	fun checkRecaptcha(serverToken: String, clientToken: String): Boolean {
		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${serverToken}&response=$clientToken")
				.body()

		val jsonParser = JsonParser.parseString(body).obj
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