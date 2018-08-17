package com.mrpowergamerbr.loritta.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CreateYouTubeWebhooksTask : Runnable {
	companion object {
		val lastNotified = ConcurrentHashMap<String, Long>()
		private val logger = KotlinLogging.logger {}
	}

	var youtubeWebhooks: MutableList<YouTubeWebhook>? = null

	override fun run() {
		try {
			// Servidores que usam o módulo do YouTube
			val servers = loritta.serversColl.find(
					Filters.gt("youTubeConfig.channels", listOf<Any>())
			)

			// IDs dos canais a serem verificados
			val channelIds = mutableSetOf<String>()

			val list = mutableListOf<ServerConfig>()

			logger.info("Verificando canais do YouTube de ${servers.count()} servidores...")

			servers.iterator().use {
				while (it.hasNext()) {
					val server = it.next()
					val guild = lorittaShards.getGuildById(server.guildId) ?: continue
					val youTubeConfig = server.youTubeConfig

					for (channel in youTubeConfig.channels) {
						if (channel.channelId == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						val textChannel = guild.getTextChannelById(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue

						channelIds.add(channel.channelId!!)
					}
					list.add(server)
				}
			}

			channelIds.forEach {
				// Caso o channel ID não esteja na map de lastNotified, vamos salvar o tempo atual nela (para evitar que anuncie coisas "do passado")
				if (!lastNotified.containsKey(it))
					lastNotified[it] = System.currentTimeMillis()
			}

			val youtubeWebhookFile = File(Loritta.FOLDER, "youtube_webhook.json")
			if (youtubeWebhooks == null && youtubeWebhookFile.exists()) {
				youtubeWebhooks = gson.fromJson(youtubeWebhookFile.readText())
			} else if (youtubeWebhooks == null) {
				youtubeWebhooks = mutableListOf()
			}

			val notCreatedYetChannels = mutableListOf<String>()

			logger.info { "Existem ${channelIds.size} canais no YouTube que eu irei verificar! Atualmente existem ${youtubeWebhooks!!.size} webhooks criadas!" }

			for (channelId in channelIds) {
				val webhook = youtubeWebhooks!!.firstOrNull { it.channelId == channelId }

				if (webhook == null) {
					notCreatedYetChannels.add(channelId)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.debug { "Webhook de ${channelId} expirou! Nós iremos recriar ela..." }
					youtubeWebhooks!!.remove(webhook)
					notCreatedYetChannels.add(channelId)
				}
			}

			logger.info { "Irei criar ${notCreatedYetChannels.size} webhooks para canais no YouTube!" }

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.map {channelId ->
				async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "unsubscribe",
										"hub.secret" to Loritta.config.mixerWebhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to Loritta.config.mixerWebhookSecret
								))
								.ok()

						// E agora realmente iremos criar!
						val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "subscribe",
										"hub.secret" to Loritta.config.mixerWebhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to Loritta.config.mixerWebhookSecret
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Erro ao tentar criar Webhook de ${channelId}! Código: ${code}" }
							return@async null
						}

						logger.debug { "Webhook de $channelId criada com sucesso! Atualmente ${webhookCount.incrementAndGet()}/${webhooksToBeCreatedCount} webhooks foram criadas!" }
						return@async YouTubeWebhook(
								channelId,
								System.currentTimeMillis(),
								432000
						)
					} catch (e: Exception) {
						logger.error("Erro ao criar subscription no YouTube", e)
						null
					}
				}
			}

			runBlocking {
				tasks.onEach {
					val webhook = it.await()

					if (webhook != null) {
						youtubeWebhooks!!.add(webhook)
					}
				}

				youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
			}
		} catch (e: Exception) {
			logger.error("Erro ao processar vídeos do YouTube", e)
		}
	}
}