package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.annotations.SerializedName
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NewLivestreamThread : Thread("Livestream Query Thread") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkNewVideos()
			} catch (e: Exception) {
				logger.error("Erro ao verificar novas streams do Twitch!", e)
			}
			Thread.sleep(5000); // Só 5s de delay!
		}
	}

	// Webhook do Mixer
	var mixerWebhook: MixerWebhook? = null

	fun checkNewVideos() {
		val mixerWebhookFile = File(Loritta.FOLDER, "mixer_webhook.json")

		if (mixerWebhook == null && mixerWebhookFile.exists())
			mixerWebhook = gson.fromJson(mixerWebhookFile.readText())

		// Servidores que usam o módulo de Livestreams
		val servers = loritta.serversColl.find(
				Filters.gt("livestreamConfig.channels", listOf<Any>())
		).toMutableList()

		logger.info("Criando webhooks de serviços de livestreams...")

		logger.info("Verificando webhooks do Mixer...")
		val channelNamePattern = Regex("mixer\\.com\\/([A-z0-9]+)").toPattern()

		val channelIds = mutableSetOf<String>()

		try {
			for (server in servers) {
				val livestreamConfig = server.livestreamConfig

				val mixerChannels = livestreamConfig.channels.filter { it.channelUrl?.startsWith("https://mixer.com/") ?: false || it.channelUrl?.startsWith("http://mixer.com/") ?: false }

				// Canais do Mixer permitem que a gente atualize uma webhook com vários eventos (yay, mágica!)
				// Ou seja, caso um novo canal seja adicionado, é melhor a gente deletar a webhook atual e criar uma nova (woosh, mágica!)
				mixerChannels.forEach {
					val channelUrl = it.channelUrl

					if (channelUrl != null) {
						val matcher = channelNamePattern.matcher(channelUrl)

						if (matcher.find()) {
							val channelName = matcher.group(1)

							// Agora nós iremos fazer um request para pegar o ID do canal, caso seja necessário
							val channelId = mixerUsernameToId.getOrPut(channelName, {
								// Okay, nós não sabemos quem é esse cara... daora a vida...
								val payload = HttpRequest.get("https://mixer.com/api/v1/channels/$channelName?fields=id")
										.acceptJson()
										.body()

								val channelId = jsonParser.parse(payload).obj["id"].nullLong

								if (channelId != null) {
									logger.info("ID do canal de ${channelName} é ${channelId}!")
									channelId
								} else {
									-1
								}
							})

							if (channelId != -1L) {
								// ID = -1 == canal inválido!
								channelIds.add(channelId.toString())
							}
						}
					}
				}
			}

			logger.info("Atualmente eu conheço ${channelIds.size} canais no Mixer!")

			val sameValues = channelIds.equals(mixerWebhook?.channelIds)

			if (!sameValues && channelIds.isNotEmpty()) {
				logger.info("O set não contém os mesmos valores! Nós iremos deletar a webhook atual e criar uma nova...")

				if (mixerWebhook != null) {
					val mixerWebhook = mixerWebhook!!
					logger.info("Desativando webhook do Mixer antigo... ${mixerWebhook.hookId}")

					HttpRequest.post("https://mixer.com/api/v1/hooks/${mixerWebhook.hookId}/deactivate")
							.acceptJson()
							.header("Client-ID", Loritta.config.mixerClientId)
							.header("Authorization", "Secret ${Loritta.config.mixerClientSecret}")
							.ok()

					logger.info("Webhook do Mixer desativado com sucesso! ${mixerWebhook.hookId}")
				}

				logger.info("Criando uma nova Webhook do Mixer!")

				val events = mutableListOf<String>()

				for (channelId in channelIds) {
					events.add("channel:$channelId:update")
				}

				val json = jsonObject(
						"kind" to "web",
						"events" to gson.toJsonTree(events),
						"url" to Loritta.config.websiteUrl + "api/v1/callbacks/mixer",
						"secret" to Loritta.config.mixerWebhookSecret
				)

				val payload = HttpRequest.post("https://mixer.com/api/v1/hooks")
						.acceptJson()
						.header("Client-ID", Loritta.config.mixerClientId)
						.header("Authorization", "Secret ${Loritta.config.mixerClientSecret}")
						.send(json.toString())
						.body()

				logger.info("Recebido ao tentar criar uma Webhook: ${payload}")
				val receivedJson = jsonParser.parse(payload).obj

				val hookId = receivedJson["id"].string
				val expiresAtString = receivedJson["expiresAt"].string
				val expiresAt = System.currentTimeMillis() + 7776000000L

				this.mixerWebhook = MixerWebhook(
						hookId,
						expiresAt
				).apply {
					this.channelIds.addAll(channelIds)
				}

				logger.info("Nova Webhook do Mixer criada com sucesso!")

				mixerWebhookFile.writeText(
						gson.toJson(mixerWebhook)
				)
			}
		} catch (e: Exception) {
			logger.error("Erro ao verificar livestreams do Mixer!", e)
		}

		logger.info("Verificando streams da Twitch... Pessoas que estão atualmente fazendo livestreams: ${isLivestreaming.joinToString(separator = ", ")}")

		// IDs dos canais a serem verificados
		var userLogins = mutableSetOf<String>()

		val list = mutableListOf<ServerConfig>()

		for (server in servers) {
			val livestreamConfig = server.livestreamConfig

			for (channel in livestreamConfig.channels) {
				if (channel.channelUrl == null && !channel.channelUrl!!.startsWith("http") && (!channel.channelUrl!!.startsWith("http://twitch.tv") && !channel.channelUrl!!.startsWith("https://twitch.tv")))
					continue

				val userLogin = channel.channelUrl!!.split("/").last()
				userLogins.add(userLogin)
			}
			list.add(server)
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
			logger.info("Verificando batch (${userLogins.size}): ${userLogins.joinToString(separator = ", ")}")
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
						logger.info("User Display Name para \"${userLogin}\" é \"$userDisplayName\"")
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

							val customTokens = mapOf(
									"game" to (gameInfo?.name ?: "???"),
									"title" to livestreamInfo.title,
									"streamer" to displayName,
									"link" to "https://www.twitch.tv/$userLogin"
							)

							textChannel.sendMessage(MessageUtils.generateMessage(message, null, guild, customTokens)).complete()
						}
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
			logger.info("Batch foi atualizada com sucesso!")
			sleep(3000)
		}

		logger.info("Usuários fazendo livestream antes: ${isLivestreaming.joinToString(separator = ", ")}")
		logger.info("Usuários fazendo livestream agora: ${nowStreaming.joinToString(separator = ", ")}")

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
		val logger = LoggerFactory.getLogger(NewLivestreamThread::class.java)

		// ===[ MIXER ]===
		val isMixerLivestreaming = mutableSetOf<String>()
		// Channel Username -> Channel ID
		val mixerUsernameToId = ConcurrentHashMap<String, Long>()

		fun getUserDisplayName(userLogin: String): String? {
			val payload = HttpRequest.get("https://api.twitch.tv/helix/users?login=${URLEncoder.encode(userLogin.trim(), "UTF-8")}")
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = jsonParser.parse(payload).obj

			try {
				val data = response["data"].array

				logger.info("getUserDisplayName payload contém ${data.size()} objetos!")

				if (data.size() == 0) {
					return null
				}

				val channel = data[0].obj
				return channel["display_name"].string
			} catch (e: IllegalStateException) {
				logger.error("Estado inválido ao manipular payload de getUserDisplayName!", e)
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

			val response = jsonParser.parse(payload).obj

			try {
				val data = response["data"].array

				logger.info("getLivestreamsInfo payload contém ${data.size()} objetos!")

				return GSON.fromJson(data)
			} catch (e: IllegalStateException) {
				logger.error("Estado inválido ao manipular payload de getLivestreamsInfo!", e)
				throw e
			}
		}

		fun getGameInfo(gameId: String): GameInfo? {
			val payload = HttpRequest.get("https://api.twitch.tv/helix/games?id=$gameId")
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = jsonParser.parse(payload).obj

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

	class MixerWebhook(
			val hookId: String, // ID do Webhook
			val expiresAt: Long // Quando deve ser renovado
	) {
		val channelIds = mutableSetOf<String>() // ID do canais
	}
}