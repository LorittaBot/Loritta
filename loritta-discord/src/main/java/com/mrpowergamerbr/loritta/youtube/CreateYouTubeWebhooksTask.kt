package com.mrpowergamerbr.loritta.youtube

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.livestreams.CreateTwitchWebhooksTask
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.TrackedYouTubeAccounts
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
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
			val allChannelIds = transaction(Databases.loritta) {
				TrackedYouTubeAccounts.slice(TrackedYouTubeAccounts.youTubeChannelId)
						.selectAll()
						.groupBy(TrackedYouTubeAccounts.youTubeChannelId)
						.toMutableList()
			}

			// IDs dos canais a serem verificados
			val channelIds = mutableSetOf<String>()
			channelIds.addAll(allChannelIds.map { it[TrackedYouTubeAccounts.youTubeChannelId] })

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

			logger.info { "There are ${channelIds.size} YouTube channels for verification! Currently there is ${youtubeWebhooks.size} created webhooks!" }

			for (channelId in channelIds) {
				val webhook = youtubeWebhooks[channelId]

				if (webhook == null) {
					notCreatedYetChannels.add(channelId)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.debug { "${channelId}'s webhook expired! We will recreate it..." }
					youtubeWebhooks.remove(channelId)
					notCreatedYetChannels.add(channelId)
				}
			}

			logger.info { "I will create ${notCreatedYetChannels.size} YouTube channel webhooks!" }

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.map {channelId ->
				GlobalScope.async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "${loritta.instanceConfig.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=ytvideo",
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
										"hub.callback" to "${loritta.instanceConfig.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "subscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to loritta.config.mixer.webhookSecret
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Something went wrong while creating ${channelId}'s webhook! Status Code: ${code}" }
							return@async null
						}

						logger.debug { "$channelId's webhook was sucessfully created! Currently there is ${webhookCount.incrementAndGet()}/${webhooksToBeCreatedCount} created webhooks!" }
						return@async Pair(
								channelId,
								YouTubeWebhook(
										System.currentTimeMillis(),
										432000
								)
						)
					} catch (e: Exception) {
						logger.error(e) { "Something went wrong when creating a YouTube subscription" }
						null
					}
				}
			}

			runBlocking {
				tasks.forEachIndexed { index, deferred ->
					val pair = deferred.await()

					if (pair != null)
						youtubeWebhooks[pair.first] = pair.second

					if (index % 50 == 0) {
						logger.info { "Saving YouTube Webhook File... $index channels were processed" }
						youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
					}
				}

				youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
			}
		} catch (e: Exception) {
			logger.error(e) { "Error while processing YouTube channels" }
		}
	}
}