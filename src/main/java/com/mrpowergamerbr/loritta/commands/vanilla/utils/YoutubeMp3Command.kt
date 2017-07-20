package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.io.StringReader
import kotlin.concurrent.fixedRateTimer


class YoutubeMp3Command : CommandBase() {
	override fun getLabel(): String {
		return "ytmp3"
	}

	override fun getUsage(): String {
		return "link"
	}

	override fun getAliases(): List<String> {
		return listOf("youtube2mp3", "youtubemp3")
	}

	override fun getDescription(locale: BaseLocale): String {
		return "Pegue o download de um vídeo do YouTube em MP3!"
	}

	override fun getExample(): List<String> {
		return listOf("https://youtu.be/BaUwnmncsrc");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "Processando...");

			var link = context.args[0]
			link = link.replace("https://www.youtube.com/watch?v=", "");
			link = link.replace("https://youtu.be/", "");

			var videoId = link;
			var callbackId = "lorittaCallback";

			var checkResponse = HttpRequest.get("https://d.yt-downloader.org/check.php?callback=$callbackId&v=$videoId&f=mp3&_=1498314662109").body().replace(callbackId, "")
			checkResponse = checkResponse.removePrefix("(").removeSuffix(")");

			val reader = StringReader(checkResponse)
			val jsonReader = JsonReader(reader)
			val checkJsonResponse = JsonParser().parse(jsonReader).asJsonObject // Base

			var hash = checkJsonResponse.get("hash").asString
			var title = checkJsonResponse.get("title").asString

			if (title == "none") {
				mensagem.editMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "Link inválido!").complete();
				return;
			}

			var lastProgress = "0";
			// create a fixed rate timer that prints hello world every 100ms
			// after a 100ms delay
			val fixedRateTimer = fixedRateTimer(name = "YTMP3 Progress Check Thread",
					initialDelay = 0, period = 1000) {
				var progressResponse = HttpRequest.get("https://d.yt-downloader.org/progress.php?callback=$callbackId&id=$hash&_=1498315402819").body().replace(callbackId, "")
				progressResponse = progressResponse.removePrefix("(").removeSuffix(")");

				val readerProgress = StringReader(progressResponse)
				val progressJsonResponse = JsonParser().parse(readerProgress).asJsonObject // Base

				val progress = progressJsonResponse.get("progress").asString;
				if (progressJsonResponse.has("error") && progressJsonResponse["error"].string.isNotEmpty()) {
					mensagem.editMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.YOUTUBEMP3_ERROR_WHEN_CONVERTING).complete()
					this.cancel()
					return@fixedRateTimer
				}
				if (progress == "1" && lastProgress != progress) {
					mensagem.editMessage("💭 **|** " + context.getAsMention(true) + "Baixando vídeo...").complete()
				}
				if (progress == "2" && lastProgress != progress) {
					mensagem.editMessage("💭 **|** " + context.getAsMention(true) + "Convertendo vídeo...").complete()
				}
				if (progress == "3") {
					var serverId = progressJsonResponse.get("sid").asString;
					var serverName = "";
					when (serverId) {
						"1" -> serverName = "fzaqn"
						"2" -> serverName = "agobe"
						"3" -> serverName = "topsa"
						"4" -> serverName = "hcqwb"
						"5" -> serverName = "gdasz"
						"6" -> serverName = "iooab"
						"7" -> serverName = "idvmg"
						"8" -> serverName = "bjtpp"
						"9" -> serverName = "sbist"
						"10" -> serverName = "gxgkr"
						"11" -> serverName = "njmvd"
						"12" -> serverName = "trciw"
						"13" -> serverName = "sjjec"
						"14" -> serverName = "puust"
						"15" -> serverName = "ocnuq"
						"16" -> serverName = "qxqnh"
						"17" -> serverName = "jureo"
						"18" -> serverName = "obdzo"
						"19" -> serverName = "wavgy"
						"20" -> serverName = "qlmqh"
						"21" -> serverName = "avatv"
						"22" -> serverName = "upajk"
						"23" -> serverName = "tvqmt"
						"24" -> serverName = "xqqqh"
						"25" -> serverName = "xrmrw"
						"26" -> serverName = "fjhlv"
						"27" -> serverName = "ejtbn"
						"28" -> serverName = "urynq"
						"29" -> serverName = "tjljs"
						"30" -> serverName = "ywjkg"
					}
					mensagem.editMessage("📥 **|** " + context.getAsMention(true) + "Pronto! Seu vídeo está pronto para ser baixado em MP3! https://$serverName.yt-downloader.org/download.php?id=$hash").complete()
					this.cancel()
				}
				lastProgress = progress;
			}
		} else {
			this.explain(context);
		}
	}
}