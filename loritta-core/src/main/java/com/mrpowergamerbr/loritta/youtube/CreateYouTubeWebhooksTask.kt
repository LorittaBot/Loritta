package com.mrpowergamerbr.loritta.youtube

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CreateYouTubeWebhooksTask : Runnable {
	companion object {
		val lastNotified = Caffeine.newBuilder().expireAfterAccess(12L, TimeUnit.HOURS).build<String, Long>().asMap()
		private val logger = KotlinLogging.logger {}
	}

	var youtubeWebhooks = mutableMapOf<String, YouTubeWebhook>()
	var fileLoaded = false

	override fun run() {
		if (!loritta.isMaster) // Não verifique caso não seja o servidor mestre
			return

		try {
			// Servidores que usam o módulo do YouTube
			val servers = loritta.serversColl.find(
					Filters.gt("youTubeConfig.channels", listOf<Any>())
			)

			// IDs dos canais a serem verificados
			val channelIds = mutableSetOf<String>()

			val list = mutableListOf<MongoServerConfig>()

			logger.info("Verificando canais do YouTube de ${servers.count()} servidores...")

			servers.iterator().use {
				while (it.hasNext()) {
					val server = it.next()
					// val guild = lorittaShards.getGuildById(server.guildId) ?: continue
					val youTubeConfig = server.youTubeConfig

					for (channel in youTubeConfig.channels) {
						if (channel.channelId == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						/* val textChannel = guild.getTextChannelByNullableId(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue */

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

			if (!fileLoaded && youtubeWebhookFile.exists()) {
				fileLoaded = true
				youtubeWebhooks = gson.fromJson(youtubeWebhookFile.readText())
			}

			val notCreatedYetChannels = mutableListOf<String>()

			logger.info { "Existem ${channelIds.size} canais no YouTube que eu irei verificar! Atualmente existem ${youtubeWebhooks!!.size} webhooks criadas!" }

			for (channelId in channelIds) {
				val webhook = youtubeWebhooks[channelId]

				if (webhook == null) {
					notCreatedYetChannels.add(channelId)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.debug { "Webhook de ${channelId} expirou! Nós iremos recriar ela..." }
					youtubeWebhooks.remove(channelId)
					notCreatedYetChannels.add(channelId)
				}
			}

			logger.info { "Irei criar ${notCreatedYetChannels.size} webhooks para canais no YouTube!" }

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.map {channelId ->
				GlobalScope.async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "unsubscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to loritta.config.mixer.webhookSecret
								))
								.ok()

						// E agora realmente iremos criar!
						val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "subscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to loritta.config.mixer.webhookSecret
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Erro ao tentar criar Webhook de ${channelId}! Código: ${code}" }
							return@async null
						}

						logger.debug { "Webhook de $channelId criada com sucesso! Atualmente ${webhookCount.incrementAndGet()}/${webhooksToBeCreatedCount} webhooks foram criadas!" }
						return@async Pair(
								channelId,
								YouTubeWebhook(
										System.currentTimeMillis(),
										432000
								)
						)
					} catch (e: Exception) {
						logger.error("Erro ao criar subscription no YouTube", e)
						null
					}
				}
			}

			runBlocking {
				tasks.onEach {
					val pair = it.await()

					if (pair != null) {
						youtubeWebhooks[pair.first] = pair.second
					}
				}

				youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
			}
		} catch (e: Exception) {
			logger.error("Erro ao processar vídeos do YouTube", e)
		}
	}
}