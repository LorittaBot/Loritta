package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class NewYouTubeVideosThread : Thread("YouTube Query Thread") {
	val doNotReverify = ConcurrentHashMap<String, Long>()
	val youTubeVideoCache = ConcurrentHashMap<String, YouTubeVideo>()
	var apiCheckTime = 0L
	var channelPlaylistIdCache = ConcurrentHashMap<String, String>()

	class YouTubeVideo(val id: String)

	override fun run() {
		super.run()

		while (true) {
			try {
				checkNewVideos()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(500); // Só 0.5s de delay!
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
		var channelIds = mutableSetOf<String>()

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
		val deferred = channelIds.map { channelId ->
			launch {
				try {
					if (!channelPlaylistIdCache.containsKey(channelId) && !doNotReverify.containsKey(channelId)) {
						if (loritta.youtubeKeys.isEmpty()) {
							return@launch
						}

						val key = loritta.youtubeKey

						var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=$channelId&key=$key")
								.body();

						var json = JSON_PARSER.parse(response).obj
						val responseError = getResponseError(json)
						val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

						if (error) {
							println("[!] Removendo key $key...")
							loritta.youtubeKeys.remove(key)
						} else {
							if (json["items"].array.size() == 0) {
								doNotReverify[channelId] = System.currentTimeMillis()
								return@launch
							}
							var playlistId = json["items"].array[0]["contentDetails"].obj.get("relatedPlaylists").asJsonObject.get("uploads").asString;

							channelPlaylistIdCache[channelId] = playlistId
						}
					}

					if (channelPlaylistIdCache[channelId] == null) {
						println("[!] Canal inválido: ${channelId} ~ Playlist privada!")
						return@launch
					}

					val playlistId = channelPlaylistIdCache[channelId]!!

					val channelStuff = HttpRequest.get("https://www.youtube.com/playlist?list=$playlistId")
							.header("Cookie", "YSC=g_0DTrOsgy8; PREF=f1=50000000&f6=7; VISITOR_INFO1_LIVE=r8qTZn_IpAs")
							.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
							.body()

					val youTubePayload = "window\\[\"ytInitialData\"\\] = (.+);".toPattern().matcher(channelStuff)

					if (!youTubePayload.find()) {
						println("[!] Canal inválido: ${channelId} ~ https://www.youtube.com/playlist?list=$playlistId")
						return@launch
					}
					val payload = JSON_PARSER.parse(youTubePayload.group(1))

					if (!payload.obj.has("contents"))
						return@launch

					try {
						val lastVideoId = payload["contents"]["twoColumnBrowseResultsRenderer"]["tabs"][0]["tabRenderer"]["content"]["sectionListRenderer"]["contents"][0]["itemSectionRenderer"]["contents"][0]["playlistVideoListRenderer"]["contents"][0]["playlistVideoRenderer"]["videoId"].string
						val lastVideoTitle = payload["contents"]["twoColumnBrowseResultsRenderer"]["tabs"][0]["tabRenderer"]["content"]["sectionListRenderer"]["contents"][0]["itemSectionRenderer"]["contents"][0]["playlistVideoListRenderer"]["contents"][0]["playlistVideoRenderer"]["title"]["simpleText"].string
						val channelName = payload["contents"]["twoColumnBrowseResultsRenderer"]["tabs"][0]["tabRenderer"]["content"]["sectionListRenderer"]["contents"][0]["itemSectionRenderer"]["contents"][0]["playlistVideoListRenderer"]["contents"][0]["playlistVideoRenderer"]["shortBylineText"]["runs"][0]["text"].string

						val lastVideo = youTubeVideoCache[channelId]

						if (lastVideo == null) {
							// Se não existe o último vídeo, então vamos salvar o novo vídeo e deixar ele lá
							val tz = TimeZone.getTimeZone("UTC")
							val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
							df.timeZone = tz
							youTubeVideoCache[channelId] = YouTubeVideo(lastVideoId)
							return@launch
						}

						if (lastVideo.id == lastVideoId)
							return@launch // É o mesmo vídeo...

						for (server in servers) {
							val youTubeConfig = server.youTubeConfig
							for (youTubeInfo in youTubeConfig.channels) {
								if (youTubeInfo.channelId == channelId) {
									val guild = lorittaShards.getGuildById(server.guildId) ?: continue

									val textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId) ?: continue

									if (!textChannel.canTalk())
										continue

									var message = youTubeInfo.videoSentMessage ?: "{link}";

									if (message.isEmpty()) {
										message = "{link}"
									}

									message = message.replace("{canal}", channelName);
									message = message.replace("{título}", lastVideoTitle);
									// message = message.replace("{descrição}", description);
									message = message.replace("{link}", "https://youtu.be/" + lastVideoId);

									textChannel.sendMessage(message.substringIfNeeded()).complete();
								}
							}
						}
						// Se chegou até aqui, então quer dizer que é um novo vídeo! :O
						youTubeVideoCache[channelId] = YouTubeVideo(lastVideoId)
					} catch (e: NoSuchElementException) {
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}

		runBlocking {
			deferred.onEach {
				it.join()
			}
			val diff = System.currentTimeMillis() - apiCheckTime
			if (diff > 60000) {
				apiCheckTime = System.currentTimeMillis()
			}
			println("${deferred.size} canais foram verificados com sucesso!")
		}
	}
}