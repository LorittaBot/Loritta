package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly
import org.slf4j.LoggerFactory
import java.io.File
import java.io.StringReader
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.fixedRateTimer

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

	fun optimizeGIF(file: File) {
		val processBuilder = ProcessBuilder(
				File(Loritta.FOLDER, "gifsicle-static").toString(), // https://github.com/kornelski/giflossy/releases
				"-i",
				file.toString(),
				"-O3",
				"--lossy=200",
				"--colors",
				"256",
				"-o",
				file.toString())

		val process = processBuilder.start()
		process.waitFor(10, TimeUnit.SECONDS)
	}
}