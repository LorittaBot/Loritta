package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.concurrent.thread


class NewYouTubeVideosThread : Thread("YouTube Query Thread") {
	val lastItemTime = ConcurrentHashMap<String, YouTubeCheck>(); // HashMap usada para guardar a data do útimo item na verificação de vídeos

	override fun run() {
		super.run()

		while (true) {
			checkNewVideos();
			Thread.sleep(10000); // Só 10s de delay!
		}
	}

	fun checkNewVideos() {
		var servers = LorittaLauncher.loritta.mongo
				.getDatabase("loritta")
				.getCollection("servers")
				.find(Filters.exists("youTubeConfig.channels", true))

		for (server in servers) {
			var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server["_id"]);

			var youTubeConfig = config.youTubeConfig;

			var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

			if (guild != null) {
				for (youTubeInfo in youTubeConfig.channels) {
					thread {
						var textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId);

						if (textChannel != null) { // Wow, diferente de null!
							if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
								if (!youTubeInfo.channelUrl!!.startsWith("http")) {
									youTubeInfo.channelUrl = "http://" + youTubeInfo.channelUrl;

									LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
								}

								if (youTubeInfo.channelId == null) { // Omg é null
									try {
										val jsoup = Jsoup.connect(youTubeInfo.channelUrl).get() // Hora de pegar a página do canal...

										val pattern = Pattern.compile("\"ucid\":\"([A-z0-9_-]+)\"");

										val matcher = pattern.matcher(jsoup.html())

										if (matcher.find()) {
											val id = matcher.group(1)

											youTubeInfo.channelId = id // E salvar o ID!
										} else {
											return@thread
										}
									} catch (e: Exception) {
										return@thread
									}
								}

								try {
									// E agora sim iremos pegar os novos vídeos!
									var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=${youTubeInfo.channelId}&key=${Loritta.config.youtubeKey}")
											.body();

									var source = "playlist";

									// ===[ INFORMAÇÕES ]===
									var title: String? = null
									var description: String? = null
									var channelTitle: String? = null
									var videoId: String? = null
									var currentCalendar: Calendar? = null
									var date: String? = null

									var playlistTitle: String? = null
									var playlistDescription: String? = null
									var playlistChannelTitle: String? = null
									var playlistVideoId: String? = null
									var playlistCalendar: Calendar? = null
									var playlistDate: String? = null

									var searchTitle: String? = null
									var searchDescription: String? = null
									var searchChannelTitle: String? = null
									var searchVideoId: String? = null
									var searchCalendar: Calendar? = null
									var searchDate: String? = null

									var rssTitle: String? = null
									var rssDescription: String? = null
									var rssChannelTitle: String? = null
									var rssVideoId: String? = null
									var rssCalendar: Calendar? = null
									var rssDate: String? = null

									var json = jsonParser.parse(response)

									// PLAYLIST
									if (json["items"].array.size() > 0) {
										var playlistId = json["items"].array[0]["contentDetails"]["relatedPlaylists"]["uploads"].string

										// Vamos verificar os novos vídeos de vários jeitos
										var newVideos = HttpRequest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&order=date&maxResults=1&playlistId=$playlistId&key=${Loritta.config.youtubeKey}")
												.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
												.useCaches(false) // YouPobre(tm)
												.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/" + Loritta.random.nextInt(50, 55) + ".0")
												.body()

										var videosJson = jsonParser.parse(newVideos);
										if (videosJson["items"].array.size() > 0) { // Se estiver vazio, quer dizer que o canal não tem vídeos!
											var jsonItem = videosJson["items"].array[0];

											var snippet = jsonItem["snippet"].obj

											playlistTitle = snippet.get("title").asString
											playlistDescription = snippet.get("description").asString
											playlistChannelTitle = snippet.get("channelTitle").asString
											playlistVideoId = snippet.get("resourceId").asJsonObject.get("videoId").asString
											playlistCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(snippet["publishedAt"].string)
											playlistDate = snippet["publishedAt"].string
										}
									}

									// SEARCH
									var newVideosSearch = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&order=date&channelId=${youTubeInfo.channelId}&key=${Loritta.config.youtubeKey}")
											.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
											.useCaches(false) // YouPobre(tm)
											.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/" + Loritta.random.nextInt(50, 55) + ".0")
											.body();

									var searchJson = jsonParser.parse(newVideosSearch);

									// Se tem erro, quer dizer que *provavelmente* o canal não existe
									if (searchJson.obj.has("error")) {
										return@thread
									}

									if (searchJson["items"].array.size() == 0) {
										return@thread
									}

									var jsonSearch = searchJson["items"].array[0]
									var searchSnippet = jsonSearch["snippet"].obj

									searchTitle = searchSnippet["title"].string
									searchDescription = searchSnippet["description"].string
									searchChannelTitle = searchSnippet["channelTitle"].string
									searchVideoId = jsonSearch["id"]["videoId"].string
									searchDate = searchSnippet["publishedAt"].string
									searchCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(searchDate)

									// RSS FEED
									var rssFeed = HttpRequest.get("https://www.youtube.com/feeds/videos.xml?channel_id=${youTubeInfo.channelId}")
											.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
											.useCaches(false) // YouPobre(tm)
											.userAgent(Constants.USER_AGENT)
											.body();

									var jsoup = Jsoup.parse(rssFeed, "", Parser.xmlParser())

									try {
										rssTitle = jsoup.select("feed entry title").first().text()
										rssDescription = jsoup.select("feed entry media|group media|description").first().text()
										rssChannelTitle = jsoup.select("feed entry author name").first().text()
										rssVideoId = jsoup.select("feed entry yt|videoId").first().text()
										rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(jsoup.select("feed entry published").first().text())

										val tz = TimeZone.getTimeZone("UTC")
										val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
										df.timeZone = tz
										rssDate = df.format(rssCalendar.time)
										rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(df.format(rssCalendar!!.time)) // Agora vamos guardar a data verdadeira!
									} catch (e: NullPointerException) {
									}

									currentCalendar = when {
										playlistCalendar != null -> playlistCalendar
										searchCalendar != null -> searchCalendar
										rssCalendar != null -> rssCalendar
										else -> null
									}

									if (currentCalendar == null) {
										println("[1] Ignorando canal ${youTubeInfo.channelUrl}...")
										return@thread
									}

									val checkedVideos = lastItemTime.getOrDefault(guild.id, YouTubeCheck());
									var lastId = checkedVideos.checked.getOrDefault(youTubeInfo.channelId, null);

									// after & equals, para que nós possamos no mínimo "preencher" os valores padrões
									if (playlistCalendar != null && (playlistCalendar.after(currentCalendar) || playlistCalendar == currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
										source = "playlist"
										date = playlistDate
										title = playlistTitle
										description = playlistDescription
										channelTitle = playlistChannelTitle
										videoId = playlistVideoId
										currentCalendar = playlistCalendar
									}

									if (searchCalendar != null && (searchCalendar.after(currentCalendar) || searchCalendar == currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
										source = "search"
										date = searchDate
										title = searchTitle
										description = searchDescription
										channelTitle = searchChannelTitle
										videoId = searchVideoId
										currentCalendar = searchCalendar
									}

									if (rssCalendar != null && (rssCalendar.after(currentCalendar) || rssCalendar == currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
										source = "rss"
										date = rssDate
										title = rssTitle
										description = rssDescription
										channelTitle = rssChannelTitle
										videoId = rssVideoId
										currentCalendar = rssCalendar
									}

									if (videoId == null || currentCalendar == null || channelTitle == null || title == null || description == null || date == null) {
										println("[2] Ignorando canal ${youTubeInfo.channelUrl}...")
										println("videoId: $videoId")
										println("currentCalendar: $currentCalendar")
										println("channelTitle: $channelTitle")
										println("title: $title")
										println("description: $description")
										println("date: $date")
										println("Source? " + source)
										return@thread
									}

									if (lastId == null) {
										// Se é null, só salve o ID do último vídeo atual e ignore!
										// E também salve o tempo atual
										val tz = TimeZone.getTimeZone("UTC")
										val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
										df.timeZone = tz
										checkedVideos.checked.put(youTubeInfo.channelId!!, Pair(videoId, df.format(Date())))
										lastItemTime[guild.id] = checkedVideos
										return@thread
									}

									val lastDate = checkedVideos.checked[guild.id]?.second
									if (lastDate != null) {
										// Data do último vídeo enviado
										val lastCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(lastDate);

										if (currentCalendar.before(lastCalendar) || currentCalendar == lastCalendar) {
											return@thread // Na verdade o vídeo atual é mais velho! Ignore então! :)
										}
									}

									if (lastId.first != videoId) {
										// Novo vídeo! Yay!
										var message = youTubeInfo.videoSentMessage;

										if (message == null) {
											return@thread
										}

										if (message.isEmpty()) {
											message = "{link}"
											youTubeInfo.videoSentMessage = message
											loritta save config
										}

										message = message.replace("{canal}", channelTitle);
										message = message.replace("{título}", title);
										message = message.replace("{descrição}", description);
										message = message.replace("{link}", "https://youtu.be/" + videoId);

										textChannel.sendMessage(message.substringIfNeeded()).complete();

										checkedVideos.checked.put(youTubeInfo.channelId!!, Pair(videoId, date))
										lastItemTime[guild.id] = checkedVideos
										println("Atualizado pela source: $source")
									}
								} catch (e: Exception) {
									println("Erro ao verificar ${youTubeInfo.channelUrl}")
									e.printStackTrace()
								}
							}
						}
					}
				}
			}
		}
	}

	data class YouTubeCheck(
			var checked: ConcurrentHashMap<String, Pair<String, String>>
	) {
		constructor() : this(ConcurrentHashMap<String, Pair<String, String>>())
	}
}