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

	fun sendYouTubeVideoMp3(context: CommandContext, videoUrl: String) {
		var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "${context.locale["PROCESSING"]}...");

		var link = videoUrl
		link = link.replace("https://www.youtube.com/watch?v=", "");
		link = link.replace("https://youtube.com/watch?v=", "");
		link = link.replace("https://m.youtube.com/watch?v=", "");
		link = link.replace("https://www.youtu.be/", "");
		link = link.replace("https://youtu.be/", "");
		link = link.replace("https://m.youtu.be/", "");
		link = link.replace("http://www.youtube.com/watch?v=", "");
		link = link.replace("http://youtube.com/watch?v=", "");
		link = link.replace("http://m.youtube.com/watch?v=", "");
		link = link.replace("http://www.youtu.be/", "");
		link = link.replace("http://youtu.be/", "");
		link = link.replace("http://m.youtu.be/", "");

		var videoId = link;
		var callbackId = "jQuery${System.currentTimeMillis()}_${System.currentTimeMillis() / 1000}"

		var checkResponse = HttpRequest.get("https://d.ymcdn.cc/check.php?callback=$callbackId&v=$videoId&f=mp3&k=Z_41_4&_=${System.currentTimeMillis() / 1000}")
				.referer("https://ytmp3.cc/")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
				.body()
				.replace(callbackId, "")

		checkResponse = checkResponse.removePrefix("(").removeSuffix(")");

		logger.info("ytmp3.cc response for \"${link}\": ${checkResponse}")
		val checkJsonResponse = try {
			JSON_PARSER.parse(checkResponse).obj // Base
		} catch (e: Exception) {
			logger.error("Erro ao processar ytmp3.cc de \"${link}\"!", e)
			throw e
		}

		var hash = checkJsonResponse["hash"].string
		var title = checkJsonResponse["title"].asString

		if (title == "none") {
			mensagem.editMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_INVALID_LINK"]).complete();
			return;
		}

		var lastProgress = "0";
		// create a fixed rate timer that prints hello world every 100ms
		// after a 100ms delay
		val fixedRateTimer = fixedRateTimer(name = "YTMP3 Progress Check Thread",
				initialDelay = 0, period = 1000) {
			var progressResponse = HttpRequest.get("https://d.ymcdn.cc/progress.php?callback=$callbackId&id=$hash&_=1498315402819")
					.userAgent(Constants.USER_AGENT)
					.body()
					.replace(callbackId, "")
			progressResponse = progressResponse.removePrefix("(").removeSuffix(")");

			val readerProgress = StringReader(progressResponse)
			val progressJsonResponse = JSON_PARSER.parse(readerProgress).obj // Base

			val progress = progressJsonResponse.get("progress").string
			if (progressJsonResponse.has("error") && progressJsonResponse["error"].string.isNotEmpty()) {
				mensagem.editMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_ERROR_WHEN_CONVERTING"]).complete()
				this.cancel()
				return@fixedRateTimer
			}
			if (progress == "1" && lastProgress != progress) {
				mensagem.editMessage("💭 **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_DOWNLOADING_VIDEO"]).complete()
			}
			if (progress == "2" && lastProgress != progress) {
				mensagem.editMessage("💭 **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_CONVERTING_VIDEO"]).complete()
			}
			if (progress == "3") {
				var serverId = progressJsonResponse.get("sid").string;
				var serverName = when (serverId) {
					"1" -> "odg"
					"2" -> "ado"
					"3" -> "jld"
					"4" -> "tzg"
					"5" -> "uuj"
					"6" -> "bkl"
					"7" -> "fnw"
					"8" -> "eeq"
					"9" -> "ebr"
					"10" -> "asx"
					"11" -> "ghn"
					"12" -> "eal"
					"13" -> "hrh"
					"14" -> "quq"
					"15" -> "zki"
					"16" -> "tff"
					"17" -> "aol"
					"18" -> "eeu"
					"19" -> "kkr"
					"20" -> "yui"
					"21" -> "yyd"
					"22" -> "hdi"
					"23" -> "ddb"
					"24" -> "iir"
					"25" -> "ihi"
					"26" -> "heh"
					"27" -> "xaa"
					"28" -> "nim"
					"29" -> "omp"
					"30" -> "eez"
					"31" -> "rpx"
					"32" -> "cxq"
					"33" -> "typ"
					"34" -> "amv"
					"35" -> "rlv"
					"36" -> "xnx"
					else -> "plz_report_this_bug_to_mrpowergamerbr"
				}
				mensagem.editMessage("📥 **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_FINISHED", "https://$serverName.ymcdn.cc/download.php?id=$hash"]).complete()
				this.cancel()
			}
			lastProgress = progress;
		}
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