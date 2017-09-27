package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.io.StringReader
import kotlin.concurrent.fixedRateTimer

object MiscUtils {
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
		var callbackId = "jQuery321008352795758680609_1506530165791"

		var checkResponse = HttpRequest.get("https://d.ymcdn.cc/check.php?callback=$callbackId&v=$videoId&f=mp3&_=1506530165795")
				.referer("https://ytmp3.cc/")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0")
				.body()
				.replace(callbackId, "")
		checkResponse = checkResponse.removePrefix("(").removeSuffix(")");

		val reader = StringReader(checkResponse)
		val jsonReader = JsonReader(reader)
		val checkJsonResponse = JsonParser().parse(jsonReader).obj // Base

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
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0")
					.body()
					.replace(callbackId, "")
			progressResponse = progressResponse.removePrefix("(").removeSuffix(")");

			val readerProgress = StringReader(progressResponse)
			val progressJsonResponse = JsonParser().parse(readerProgress).obj // Base

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
				var serverName = "";
				when (serverId) {
					"1" -> serverName = "odg"
					"2" -> serverName = "ado"
					"3" -> serverName = "jld"
					"4" -> serverName = "tzg"
					"5" -> serverName = "uuj"
					"6" -> serverName = "bkl"
					"7" -> serverName = "fnw"
					"8" -> serverName = "eeq"
					"9" -> serverName = "ebr"
					"10" -> serverName = "asx"
					"11" -> serverName = "ghn"
					"12" -> serverName = "eal"
					"13" -> serverName = "hrh"
					"14" -> serverName = "quq"
					"15" -> serverName = "zki"
					"16" -> serverName = "tff"
					"17" -> serverName = "aol"
					"18" -> serverName = "eeu"
					"19" -> serverName = "kkr"
					"20" -> serverName = "yui"
					"21" -> serverName = "yyd"
					"22" -> serverName = "hdi"
					"23" -> serverName = "ddb"
					"24" -> serverName = "iir"
					"25" -> serverName = "ihi"
					"26" -> serverName = "heh"
					"27" -> serverName = "xaa"
					"28" -> serverName = "nim"
					"29" -> serverName = "omp"
					"30" -> serverName = "eez"
				}
				mensagem.editMessage("📥 **|** " + context.getAsMention(true) + context.locale["YOUTUBEMP3_FINISHED", "https://$serverName.ymcdn.cc/download.php?id=$hash"]).complete()
				this.cancel()
			}
			lastProgress = progress;
		}
	}
}