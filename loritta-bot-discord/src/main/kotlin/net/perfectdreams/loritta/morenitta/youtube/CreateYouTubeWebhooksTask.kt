package net.perfectdreams.loritta.morenitta.youtube

import com.github.salomonbrys.kotson.fromJson
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.MiscellaneousData
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.gson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.util.concurrent.atomic.AtomicInteger

class CreateYouTubeWebhooksTask(val loritta: LorittaBot) : RunnableCoroutine {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
		const val DATA_KEY = "youtube_webhooks"
	}

	var youtubeWebhooks = mutableMapOf<String, YouTubeWebhook>()
	var fileLoaded = false

	override suspend fun run() {
		try {
			// Servidores que usam o módulo do YouTube
			val allChannelIds = loritta.pudding.transaction {
				TrackedYouTubeAccounts.select(TrackedYouTubeAccounts.youTubeChannelId)
					.groupBy(TrackedYouTubeAccounts.youTubeChannelId)
					.toMutableList()
			}

			// IDs dos canais a serem verificados
			val channelIds = mutableSetOf<String>()
			channelIds.addAll(allChannelIds.map { it[TrackedYouTubeAccounts.youTubeChannelId] })

			if (!fileLoaded) {
				val youTubeWebhooksData = loritta.newSuspendedTransaction {
					MiscellaneousData.selectAll().where { MiscellaneousData.id eq DATA_KEY }
						.limit(1)
						.firstOrNull()
						?.get(MiscellaneousData.data)
				}

				fileLoaded = true
				if (youTubeWebhooksData != null) {
					youtubeWebhooks = gson.fromJson(youTubeWebhooksData)
				}
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
					logger.info { "${channelId}'s webhook expired! We will recreate it..." }
					youtubeWebhooks.remove(channelId)
					notCreatedYetChannels.add(channelId)
				}
			}

			logger.info { "I will create ${notCreatedYetChannels.size} YouTube channel webhooks!" }

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.map { channelId ->
				GlobalScope.async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						// Vamos criar!
						val response = loritta.http.post("https://pubsubhubbub.appspot.com/subscribe") {
							setBody(
								FormDataContent(
									Parameters.build {
										append("hub.callback", "${loritta.config.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=ytvideo")
										append("hub.lease_seconds", "")
										append("hub.mode", "subscribe")
										append("hub.secret", loritta.config.loritta.webhookSecret)
										append("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId")
										append("hub.verify", "async")
										append("hub.verify_token", loritta.config.loritta.webhookSecret)
									}
								)
							)
						}
						val code = response.status

						if (code != HttpStatusCode.NoContent && code != HttpStatusCode.Accepted) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Something went wrong while creating ${channelId}'s webhook! Status Code: ${code}" }
							return@async null
						}

						// We need to put them outside of the .info {} block since we *need* them to be incremented
						val incrementAndGet = webhookCount.incrementAndGet()
						logger.info { "$channelId's webhook was sucessfully created! Currently there is $incrementAndGet/${webhooksToBeCreatedCount} created webhooks!" }

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

			tasks.forEachIndexed { index, deferred ->
				val pair = deferred.await()

				if (pair != null)
					youtubeWebhooks[pair.first] = pair.second

				if (index % 50 == 0 && index != 0) { // Do not write the file if index == 0, because it would be a *very* unnecessary write
					logger.info { "Saving YouTube Webhook File... $index channels were processed" }
					loritta.newSuspendedTransaction {
						MiscellaneousData.upsert(MiscellaneousData.id) {
							it[MiscellaneousData.id] = DATA_KEY
							it[MiscellaneousData.data] = gson.toJson(youtubeWebhooks)
						}
					}
				}
			}

			val createdWebhooksCount = webhookCount.get()

			if (createdWebhooksCount != 0) {
				loritta.newSuspendedTransaction {
					MiscellaneousData.upsert(MiscellaneousData.id) {
						it[MiscellaneousData.id] = DATA_KEY
						it[MiscellaneousData.data] = gson.toJson(youtubeWebhooks)
					}
				}

				logger.info { "Successfully wrote YouTube Webhook File! ${webhookCount.get()} channels were processed" }
			} else {
				logger.info { "Successfully finished YouTube Webhook Task! No new webhooks were created..." }
			}
		} catch (e: Exception) {
			logger.error(e) { "Error while processing YouTube channels" }
		}
	}
}