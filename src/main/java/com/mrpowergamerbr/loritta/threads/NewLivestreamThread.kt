package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.annotations.SerializedName
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.debug.DebugType
import com.mrpowergamerbr.loritta.utils.debug.debug
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class NewLivestreamThread : Thread("Livestream Query Thread") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkNewVideos()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000); // Só 5s de delay!
		}
	}

	fun checkNewVideos() {
		debug(DebugType.TWITCH_THREAD, "Checking Twitch streams... ${isLivestreaming.joinToString(separator = ", ")}")

		// Servidores que usam o módulo do Twitch
		val servers = loritta.serversColl.find(
				Filters.gt("livestreamConfig.channels", listOf<Any>())
		).iterator()

		// IDs dos canais a serem verificados
		var userLogins = mutableSetOf<String>()

		val list = mutableListOf<ServerConfig>()

		servers.use {
			while (it.hasNext()) {
				val server = it.next()
				val livestreamConfig = server.livestreamConfig

				for (channel in livestreamConfig.channels) {
					if (channel.channelUrl == null && !channel.channelUrl!!.startsWith("http"))
						continue

					val userLogin = channel.channelUrl!!.split("/").last()
					userLogins.add(userLogin)
				}
				list.add(server)
			}
		}

		// Vamos criar uma "lista" de IDs para serem procurados (batching)
		val batchs = mutableListOf<ArrayList<String>>()

		var currentBatch = arrayListOf<String>()

		for (userLogin in userLogins) {
			if (currentBatch.size == 100) {
				batchs.add(currentBatch)
				currentBatch = arrayListOf<String>()
			}
			currentBatch.add(userLogin)
		}

		batchs.add(currentBatch)

		val nowStreaming = mutableSetOf<String>()

		// Agora iremos verificar os canais
		batchs.forEach { userLogins ->
			debug(DebugType.TWITCH_THREAD, "Verifying batch ${userLogins.joinToString(separator = ", ")}")
			try {
				val livestreamsInfo = getLivestreamsInfo(userLogins)

				for (livestreamInfo in livestreamsInfo) {
					val userLogin = livestreamInfo.thumbnailUrl.substring(52 until livestreamInfo.thumbnailUrl.lastIndexOf('-')) // ouc
					nowStreaming.add(userLogin)

					if (isLivestreaming.contains(userLogin)) // Se o usuário já está fazendo livestream, não vamos querer saber a mesma coisa novamente, né?
						continue

					if (!gameInfoCache.containsKey(livestreamInfo.gameId)) {
						val gameInfo = getGameInfo(livestreamInfo.gameId)

						if (gameInfo != null) {
							gameInfoCache[livestreamInfo.gameId] = gameInfo
						}
					}

					val gameInfo = gameInfoCache[livestreamInfo.gameId]

					val displayName = if (displayNameCache.containsKey(userLogin)) {
						displayNameCache[userLogin]!!
					} else {
						val userDisplayName = getUserDisplayName(userLogin)
						debug(DebugType.TWITCH_THREAD, "User Display Name for ${userLogin} is $userDisplayName")
						val channelName = userDisplayName ?: continue
						displayNameCache[userLogin] = channelName
						channelName
					}

					for (server in list) {
						val livestreamConfig = server.livestreamConfig

						val channels = livestreamConfig.channels.filter {
							val channelUserLogin = it.channelUrl!!.split("/").last()

							userLogin == channelUserLogin
						}

						for (channel in channels) {
							val guild = lorittaShards.getGuildById(server.guildId) ?: continue

							val textChannel = guild.getTextChannelById(channel.repostToChannelId) ?: continue

							if (!textChannel.canTalk())
								continue

							var message = channel.videoSentMessage ?: "{link}";

							if (message.isEmpty()) {
								message = "{link}"
							}

							message = message.replace("{game}", gameInfo?.name ?: "???")
							message = message.replace("{title}", livestreamInfo.title)
							message = message.replace("{streamer}", displayName)
							message = message.replace("{link}", "https://www.twitch.tv/$userLogin")

							textChannel.sendMessage(message.substringIfNeeded()).complete();
						}
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
			debug(DebugType.TWITCH_THREAD, "Finished updating batch! ")
			sleep(3000)
		}

		debug(DebugType.TWITCH_THREAD, "LIVESTREAMING BEFORE: ${isLivestreaming.joinToString(separator = ", ")}")
		debug(DebugType.TWITCH_THREAD, "LIVESTREAMING NOW: ${nowStreaming.joinToString(separator = ", ")}")

		isLivestreaming.clear()

		nowStreaming.forEach {
			isLivestreaming.add(it)
		}

		File(Loritta.FOLDER, "livestreaming.json").writeText(GSON.toJson(isLivestreaming))
	}

	companion object {
		var isLivestreaming = mutableSetOf<String>()
		val gameInfoCache = ConcurrentHashMap<String, GameInfo>()
		val displayNameCache = ConcurrentHashMap<String, String>()

		fun getUserDisplayName(userLogin: String): String? {
			val payload = HttpRequest.get("https://api.twitch.tv/helix/users?login=${URLEncoder.encode(userLogin.trim(), "UTF-8")}")
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = JSON_PARSER.parse(payload).obj

			try {
				val data = response["data"].array

				debug(DebugType.TWITCH_THREAD, "getUserDisplayName payload response contains ${data.size()} objects!")

				if (data.size() == 0) {
					return null
				}

				val channel = data[0].obj
				return channel["display_name"].string
			} catch (e: IllegalStateException) {
				debug(DebugType.TWITCH_THREAD, payload)
				return null
			}
		}

		fun getLivestreamsInfo(userLogins: List<String>): List<LivestreamInfo> {
			var query = ""
			userLogins.forEach {
				if (query.isEmpty()) {
					query += "?user_login=${URLEncoder.encode(it.trim(), "UTF-8")}"
				} else {
					query += "&user_login=${URLEncoder.encode(it.trim(), "UTF-8")}"
				}
			}
			val url = "https://api.twitch.tv/helix/streams$query"
			val payload = HttpRequest.get(url)
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = JSON_PARSER.parse(payload).obj

			try {
				val data = response["data"].array

				debug(DebugType.TWITCH_THREAD, "getLivestreamsInfo payload response contains ${data.size()} objects!")

				return GSON.fromJson(data)
			} catch (e: java.lang.IllegalStateException) {
				debug(DebugType.TWITCH_THREAD, payload)
				throw e
			}
		}

		fun getGameInfo(gameId: String): GameInfo? {
			val payload = HttpRequest.get("https://api.twitch.tv/helix/games?id=$gameId")
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = JSON_PARSER.parse(payload).obj

			val data = response["data"].array

			if (data.size() == 0) {
				return null
			}

			val channel = data[0].obj

			return GSON.fromJson(channel)
		}

		class LivestreamInfo(
				val id: String,
				@SerializedName("user_id")
				val userId: String,
				@SerializedName("game_id")
				val gameId: String,
				@SerializedName("community_ids")
				val communityIds: List<String>,
				val type: String,
				val title: String,
				@SerializedName("viewer_count")
				val viewerCount: Long,
				@SerializedName("started_at")
				val startedAt: String,
				val language: String,
				@SerializedName("thumbnail_url")
				val thumbnailUrl: String
		)

		class GameInfo(
				@SerializedName("box_art_url")
				val boxArtUrl: String,
				val id: String,
				val name: String
		)
	}
}