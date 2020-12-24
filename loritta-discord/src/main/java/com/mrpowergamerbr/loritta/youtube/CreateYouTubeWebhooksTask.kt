package com.mrpowergamerbr.loritta.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class CreateYouTubeWebhooksTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var youtubeWebhooks = mutableMapOf<String, YouTubeWebhook>()
	var fileLoaded = false

	override fun run() {
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
						// Vamos criar!
						val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "${loritta.instanceConfig.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=ytvideo",
										"hub.lease_seconds" to "",
										"hub.mode" to "subscribe",
										"hub.secret" to loritta.config.generalWebhook.webhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to loritta.config.generalWebhook.webhookSecret
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Something went wrong while creating ${channelId}'s webhook! Status Code: ${code}" }
							return@async null
						}

						// We need to put them outside of the .debug {} block since we *need* them to be incremented
						val incrementAndGet = webhookCount.incrementAndGet()
						logger.debug { "$channelId's webhook was sucessfully created! Currently there is $incrementAndGet/${webhooksToBeCreatedCount} created webhooks!" }

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

					if (index % 50 == 0 && index != 0) { // Do not write the file if index == 0, because it would be a *very* unnecessary write
						logger.info { "Saving YouTube Webhook File... $index channels were processed" }
						youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
					}
				}

				val createdWebhooksCount = webhookCount.get()

				if (createdWebhooksCount != 0) {
					youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))

					logger.info { "Successfully wrote YouTube Webhook File! ${webhookCount.get()} channels were processed" }
				} else {
					logger.info { "Successfully finished YouTube Webhook Task! No new webhooks were created..." }
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Error while processing YouTube channels" }
		}
	}
}