package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class NewYouTubeVideosThread : Thread("YouTube Query Thread") {
	val doNotReverify = ConcurrentHashMap<String, Long>()
	val youTubeVideoCache = ConcurrentHashMap<String, YouTubeVideo>()

	class YouTubeVideo(val id: String, val date: String)

	override fun run() {
		super.run()

		while (true) {
			try {
				checkNewVideos()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(7500); // Só 7.5s de delay!
		}
	}

	fun getResponseError(json: JsonObject): String? {
		if (!json.has("error"))
			return null

		return json["error"]["errors"][0]["reason"].string
	}

	fun checkNewVideos() {
		// Servidores que usam o módulo do YouTube
		val servers = loritta.ds.find(ServerConfig::class.java).field("youTubeConfig.channels").exists()
		// IDs dos canais a serem verificados
		val channelIds = mutableSetOf<String>()

		for (server in servers) {
			val youTubeConfig = server.youTubeConfig

			for (channel in youTubeConfig.channels) {
				if (channel.channelId == null)
					continue
				if (channel.channelUrl == null && !channel.channelUrl!!.startsWith("http"))
					continue
				channelIds.add(channel.channelId!!)
			}
		}

		// Agora iremos verificar os canais
		for (channelId in channelIds) {
			try {
				if (doNotReverify.containsKey(channelId)) {
					if (System.currentTimeMillis() - doNotReverify[channelId]!! > 1.8e+6) {
						doNotReverify.remove(channelId)
					} else {
						continue
					}
				}

				var source = "playlist";

				// ===[ INFORMAÇÕES ]===
				var title: String? = null
				var description: String? = null
				var channelTitle: String? = null
				var videoId: String? = null
				var currentCalendar: Calendar? = null
				var date: String? = null

				/* var playlistTitle: String? = null
				   var playlistDescription: String? = null
			var playlistChannelTitle: String? = null
			var playlistVideoId: String? = null
			var playlistCalendar: Calendar? = null
			var playlistDate: String? = null */

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

				// E agora sim iremos pegar os novos vídeos!
				// ===[ SEARCH ]===
				// SEARCH
				if (loritta.youtubeKeys.isNotEmpty()) {
					val key = loritta.youtubeKey
					var newVideosSearchResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&order=date&channelId=${channelId}&key=${key}")
							.header("Cache-Control", "max-age=0, no-cache") // YouPobre(tm)
							.useCaches(false) // YouPobre(tm)
							.userAgent(Constants.USER_AGENT)
							.body();

					var searchJson = JSON_PARSER.parse(newVideosSearchResponse).obj
					val responseError = getResponseError(searchJson)
					val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

					if (error) {
						println("[!] Removendo key $key...")
						loritta.youtubeKeys.remove(key)
					} else {
						try {
							if (searchJson["items"].array.size() > 0) {
								var jsonSearch = searchJson["items"].array[0]
								var searchSnippet = jsonSearch["snippet"].obj

								searchTitle = searchSnippet["title"].string
								searchDescription = searchSnippet["description"].string
								searchChannelTitle = searchSnippet["channelTitle"].string
								searchVideoId = jsonSearch["id"]["videoId"].string
								searchDate = searchSnippet["publishedAt"].string
								searchCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(searchDate)
							}
						} catch (e: Exception) {
							println(newVideosSearchResponse)
						}
					}
				}

				// ===[ RSS FEEDS ]==
				var rssFeed = HttpRequest.get("https://www.youtube.com/feeds/videos.xml?channel_id=${channelId}")
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
					rssDate = df.format(rssCalendar!!.time)
					rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(df.format(rssCalendar!!.time)) // Agora vamos guardar a data verdadeira!
				} catch (e: NullPointerException) {
				}

				currentCalendar = when {
					searchCalendar != null -> searchCalendar
					rssCalendar != null -> rssCalendar
					else -> null
				}

				if (currentCalendar == null) {
					println("[1] Ignorando canal ${channelId}...")
					doNotReverify[channelId] = System.currentTimeMillis()
					continue
				}

				if (searchCalendar != null && ((searchCalendar as Calendar).after(currentCalendar) || searchCalendar == currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
					source = "search"
					date = searchDate
					title = searchTitle
					description = searchDescription
					channelTitle = searchChannelTitle
					videoId = searchVideoId
					currentCalendar = searchCalendar
				}

				if (rssCalendar != null && ((rssCalendar as Calendar).after(currentCalendar) || rssCalendar == currentCalendar)) { // Se o vídeo do search endpoint é mais recente que o da playlist...
					source = "rss"
					date = rssDate
					title = rssTitle
					description = rssDescription
					channelTitle = rssChannelTitle
					videoId = rssVideoId
					currentCalendar = rssCalendar
				}

				if (videoId == null || currentCalendar == null || channelTitle == null || title == null || description == null || date == null) {
					println("[2] Ignorando canal ${channelId}...")
					println("videoId: $videoId")
					println("currentCalendar: $currentCalendar")
					println("channelTitle: $channelTitle")
					println("title: $title")
					println("description: $description")
					println("date: $date")
					println("Source? " + source)
					continue
				}

				val lastVideo = youTubeVideoCache[channelId]

				if (lastVideo == null) {
					// Se não existe o último vídeo, então vamos salvar o novo vídeo e deixar ele lá
					val tz = TimeZone.getTimeZone("UTC")
					val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
					df.timeZone = tz
					youTubeVideoCache[channelId] = YouTubeVideo(videoId, df.format(Date()))
					continue
				}

				if (lastVideo.id == videoId)
					continue // É o mesmo vídeo...

				// Data do último vídeo enviado
				val lastCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(lastVideo.date);

				if (currentCalendar.before(lastCalendar) || currentCalendar == lastCalendar) {
					continue // Na verdade o vídeo atual é mais velho! Ignore então! :)
				}

				for (server in servers) {
					val youTubeConfig = server.youTubeConfig
					for (youTubeInfo in youTubeConfig.channels) {
						if (youTubeInfo.channelId == channelId) {
							val guild = lorittaShards.getGuildById(server.guildId)
							if (guild == null)
								continue

							val textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId)
							if (textChannel == null)
								continue
							if (!textChannel.canTalk())
								continue

							var message = youTubeInfo.videoSentMessage ?: "{link}";

							if (message.isEmpty()) {
								message = "{link}"
							}

							message = message.replace("{canal}", channelTitle);
							message = message.replace("{título}", title);
							message = message.replace("{descrição}", description);
							message = message.replace("{link}", "https://youtu.be/" + videoId);

							textChannel.sendMessage(message.substringIfNeeded()).complete();
						}
					}
				}
				// Se chegou até aqui, então quer dizer que é um novo vídeo! :O
				youTubeVideoCache[channelId] = YouTubeVideo(videoId, date)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}