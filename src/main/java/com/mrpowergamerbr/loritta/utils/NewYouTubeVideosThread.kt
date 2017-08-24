package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.*


class NewYouTubeVideosThread : Thread("YouTube Query Thread") {
	// val lastVideos = HashMap<String, String>(); // HashMap usada para guardar o ID do último vídeo
	// val lastVideosTime = HashMap<String, String>(); // HashMap usada para guardar a data do último vídeo
	val lastItemTime = HashMap<String, NewYouTubeVideosThread.YouTubeCheck>(); // HashMap usada para guardar a data do útimo item na RSS

	override fun run() {
		super.run()

		while (true) {
			checkNewVideos();
			Thread.sleep(10000); // Só 10s de delay!
		}
	}

	fun checkNewVideos() {
		try {
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.exists("youTubeConfig.channels", true))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server.get("_id"));

				var youTubeConfig = config.youTubeConfig;

				var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

				if (guild != null) {
					for (youTubeInfo in youTubeConfig.channels) {
						var textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId);

						if (textChannel != null) { // Wow, diferente de null!
							if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
								if (!youTubeInfo.channelUrl!!.startsWith("http")) {
									youTubeInfo.channelUrl = "http://" + youTubeInfo.channelUrl;

									LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
								}
								if (youTubeInfo.channelId == null) { // Omg é null
									try {
										var jsoup = Jsoup.connect(youTubeInfo.channelUrl).get() // Hora de pegar a página do canal...

										var id = jsoup.getElementsByAttribute("data-channel-external-id")[0].attr("data-channel-external-id"); // Que possuem o atributo "data-channel-external-id" (que é o ID do canal)

										youTubeInfo.channelId = id; // E salvar o ID!

										LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
									} catch (e: Exception) {
										continue;
									}
								}

								// E agora sim iremos pegar os novos vídeos!
								var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=${youTubeInfo.channelId}&key=${Loritta.config.youtubeKey}")
										.body();

								var parser = JsonParser();
								var json = parser.parse(response);
								var playlistId = json.get("items").asJsonArray[0].get("contentDetails").asJsonObject.get("relatedPlaylists").asJsonObject.get("uploads").asString;

								var source = "playlist";

								// Vamos verificar os novos vídeos de vários jeitos
								var newVideos = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&order=date&maxResults=1&playlistId=$playlistId&key=${Loritta.config.youtubeKey}")
										.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
										.useCaches(false) // YouPobre(tm)
										.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/" + Loritta.random.nextInt(50, 55) + ".0")
										.body();

								var newVideosSearch = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&order=date&channelId=${youTubeInfo.channelId}&key=${Loritta.config.youtubeKey}")
										.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
										.useCaches(false) // YouPobre(tm)
										.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/" + Loritta.random.nextInt(50, 55) + ".0")
										.body();

								var rssFeed = HttpRequest.get("https://www.youtube.com/feeds/videos.xml?channel_id=${youTubeInfo.channelId}")
										.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
										.useCaches(false) // YouPobre(tm)
										.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/" + Loritta.random.nextInt(50, 55) + ".0")
										.body();

								var jsoup = Jsoup.parse(rssFeed, "", Parser.xmlParser())

								var videosJson = JsonParser().parse(newVideos);
								if (videosJson["items"].array.size() == 0) { // Se estiver vazio, quer dizer que o canal não tem vídeos!
									continue;
								}
								var jsonItem = videosJson.get("items").asJsonArray[0];

								var searchJson = JsonParser().parse(newVideosSearch);
								var jsonSearch = searchJson.get("items").asJsonArray[0];

								val checkedVideos = lastItemTime.getOrDefault(guild.id, NewYouTubeVideosThread.YouTubeCheck());
								var lastId = checkedVideos.checked.getOrDefault(youTubeInfo.channelId, null);

								var snippet = jsonItem.get("snippet").asJsonObject
								var searchSnippet = jsonSearch.get("snippet").asJsonObject

								var title = snippet.get("title").asString;
								var description = snippet.get("description").asString;
								var channelTitle = snippet.get("channelTitle").asString;
								var videoId = snippet.get("resourceId").asJsonObject.get("videoId").asString;

								var datePlaylist = snippet["publishedAt"].string;
								var dateSearch = searchSnippet["publishedAt"].string;
								var dateRss = jsoup.select("feed entry published").first().text()
								var date: String = datePlaylist;

								var playlistCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(datePlaylist);
								var searchCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateSearch);
								var rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateRss);

								var currentCalendar = playlistCalendar;

								val tz = TimeZone.getTimeZone("UTC")
								val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
								df.timeZone = tz
								dateRss = df.format(rssCalendar.time) // Agora vamos guardar a data verdadeira!

								if (searchCalendar.after(currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
									source = "search"
									date = dateSearch;
									title = searchSnippet.get("title").asString;
									description = searchSnippet.get("description").asString;
									channelTitle = searchSnippet.get("channelTitle").asString;
									videoId = jsonSearch.get("id").asJsonObject.get("videoId").asString;
									currentCalendar = searchCalendar
								}

								if (rssCalendar.after(currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
									source = "rss"
									date = dateRss;
									title = jsoup.select("feed entry title").first().text()
									description = jsoup.select("feed entry media|group media|description").first().text()
									channelTitle = jsoup.select("feed entry author name").first().text()
									videoId = jsoup.select("feed entry yt|videoId").first().text()
									currentCalendar = rssCalendar
								}

								if (lastId == null) {
									// Se é null, só salve o ID do último vídeo atual e ignore!
									// E também salve o tempo atual
									val tz = TimeZone.getTimeZone("UTC")
									val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
									df.timeZone = tz
									checkedVideos.checked.put(youTubeInfo.channelId!!, Pair(videoId, df.format(Date())))
									lastItemTime[guild.id] = checkedVideos
									continue;
								}

								val lastDate = checkedVideos?.checked?.get(guild.id)?.second;
								if (lastDate != null) {
									// Data do último vídeo enviado
									val lastCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(lastDate);

									if (currentCalendar.before(lastCalendar) || currentCalendar.equals(lastCalendar)) {
										continue; // Na verdade o vídeo atual é mais velho! Ignore então! :)
									}
								}

								if (lastId.first != videoId) {
									// Novo vídeo! Yay!
									var message = youTubeInfo.videoSentMessage;

									if (message == null) {
										continue; }

									if (message.isEmpty()) {
										message = "{link}"
										youTubeInfo.videoSentMessage = message
										loritta save config
									}

									message = message.replace("{canal}", channelTitle);
									message = message.replace("{título}", title);
									message = message.replace("{descrição}", description);
									message = message.replace("{link}", "https://youtu.be/" + videoId);

									textChannel.sendMessage(message).complete();

									checkedVideos.checked.put(youTubeInfo.channelId!!, Pair(videoId, date))
									lastItemTime[guild.id] = checkedVideos
									println("Atualizado pela source: $source")
								}
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	data class YouTubeCheck(
			var checked: HashMap<String, Pair<String, String>>
	) {
		constructor() : this(HashMap<String, Pair<String, String>>())
	}
}