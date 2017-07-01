package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import org.jsoup.Jsoup

class NewYouTubeVideosThread : Thread() {
	val lastVideos = HashMap<String, String>(); // HashMap usada para guardar o ID do último vídeo

	override fun run() {
		super.run()

		while (true) {
			checkNewVideos();
			Thread.sleep(2500); // Só 2.5s de delay!
		}
	}

	fun checkNewVideos() {
		try {
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.eq("youTubeConfig.isEnabled", true))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server.get("_id"));

				var youTubeConfig = config.youTubeConfig;

				if (youTubeConfig.isEnabled) { // Está ativado?
					var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

					if (guild != null) {
						var textChannel = guild.getTextChannelById(youTubeConfig.repostToChannelId);

						if (textChannel != null) { // Wow, diferente de null!
							if (!youTubeConfig.channelUrl!!.startsWith("http")) {
								youTubeConfig.channelUrl = "http://" + youTubeConfig.channelUrl;

								LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
							}
							if (youTubeConfig.channelId == null) { // Omg é null
								try {
									var jsoup = Jsoup.connect(youTubeConfig.channelUrl).get() // Hora de pegar a página do canal...

									var id = jsoup.getElementsByAttribute("data-channel-external-id")[0].attr("data-channel-external-id"); // Que possuem o atributo "data-channel-external-id" (que é o ID do canal)

									youTubeConfig.channelId = id; // E salvar o ID!

									LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
								} catch (e: Exception) {
									// Se deu ruim, desative o módulo!
									youTubeConfig.isEnabled = false;
									LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
									continue;
								}
							}
							// E agora sim iremos pegar os novos vídeos!
							var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=${youTubeConfig.channelId}&key=${Loritta.config.youtubeKey}")
									.body();

							var parser = JsonParser();
							var json = parser.parse(response);
							var playlistId = json.get("items").asJsonArray[0].get("contentDetails").asJsonObject.get("relatedPlaylists").asJsonObject.get("uploads").asString;

							var newVideos = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=3&playlistId=$playlistId&key=${Loritta.config.youtubeKey}")
									.body();

							var videosJson = parser.parse(newVideos);
							var jsonItem = videosJson.get("items").asJsonArray[0];

							var lastId = lastVideos.getOrDefault(guild.id, null);

							var snippet = jsonItem.get("snippet").asJsonObject

							var title = snippet.get("title").asString;
							var description = snippet.get("description").asString;
							var channelTitle = snippet.get("channelTitle").asString;
							var videoId = snippet.get("resourceId").asJsonObject.get("videoId").asString;

							if (lastId == null) {
								// Se é null, só salve o ID do último vídeo atual e ignore!
								lastVideos.put(guild.id, videoId);
								continue;
							} else if (lastId != videoId) {
								// Novo vídeo! Yay!
								var message = youTubeConfig.videoSentMessage;

								if (message == null) { continue; }

								message = message.replace("{canal}", channelTitle);
								message = message.replace("{título}", title);
								message = message.replace("{descrição}", description);
								message = message.replace("{link}", "https://youtu.be/" + videoId);

								textChannel.sendMessage(message).complete();

								lastVideos.put(guild.id, videoId);
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}