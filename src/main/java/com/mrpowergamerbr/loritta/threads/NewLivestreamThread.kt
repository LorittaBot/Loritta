package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.annotations.SerializedName
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.livestreams.TwitchUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import org.slf4j.LoggerFactory
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

					val deactivatedBody = HttpRequest.post("https://mixer.com/api/v1/hooks/${mixerWebhook.hookId}/deactivate")
							.acceptJson()
							.header("Client-ID", Loritta.config.mixerClientId)
							.header("Authorization", "Secret ${Loritta.config.mixerClientSecret}")
							.send("{}") // Como é um POST, nós precisamos enviar alguma coisa, caso contrário, irá dar erro
							.body()

					logger.info("Webhook do Mixer desativado com sucesso! ${mixerWebhook.hookId} ${deactivatedBody}")
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

				if (receivedJson["errorCode"].nullInt == 4010) {
					// Quer dizer que já estamos escutando por invites deste tipo... hmmmm...
					return
				}
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
			val payload = TwitchUtils.makeTwitchApiRequest("https://api.twitch.tv/helix/users?login=${URLEncoder.encode(userLogin.trim(), "UTF-8")}").body()

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

		fun getGameInfo(gameId: String): GameInfo? {
			val payload = TwitchUtils.makeTwitchApiRequest("https://api.twitch.tv/helix/games?id=$gameId")
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