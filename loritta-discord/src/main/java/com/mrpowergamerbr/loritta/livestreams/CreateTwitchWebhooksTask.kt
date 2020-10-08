package com.mrpowergamerbr.loritta.livestreams

import com.github.benmanes.caffeine.cache.Caffeine
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
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.twitch.TwitchAPI
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CreateTwitchWebhooksTask : Runnable {
	companion object {
		val lastNotified = Caffeine.newBuilder().expireAfterAccess(12L, TimeUnit.HOURS).build<Long, Long>().asMap()
		private val logger = KotlinLogging.logger {}

		fun getWebhookRegisteringTwitchApiForUser(userId: Long): TwitchAPI {
			// Fuck Twitch
			return when (userId.rem(8)) {
				7L -> loritta.twitch8
				6L -> loritta.twitch7
				5L -> loritta.twitch6
				4L -> loritta.twitch5
				3L -> loritta.twitch4
				2L -> loritta.twitch3
				1L -> loritta.twitch2
				else -> loritta.twitch
			}
		}
	}

	var twitchWebhooks = mutableMapOf<Long, TwitchWebhook>()
	var fileLoaded = false

	override fun run() {
		try {
			val allChannelIds = transaction(Databases.loritta) {
				TrackedTwitchAccounts.slice(TrackedTwitchAccounts.twitchUserId)
						.selectAll()
						.groupBy(TrackedTwitchAccounts.twitchUserId)
						.toMutableList()
			}

			// User Logins dos canais a serem verificados
			val userIds = mutableSetOf<Long>()
			userIds.addAll(allChannelIds.map { it[TrackedTwitchAccounts.twitchUserId] })

			val twitchWebhookFile = File(Loritta.FOLDER, "twitch_webhook.json")

			if (!fileLoaded && twitchWebhookFile.exists()) {
				fileLoaded = true
				twitchWebhooks = gson.fromJson(twitchWebhookFile.readText())
			}

			val notCreatedYetChannels = mutableListOf<Long>()

			logger.info { "There are ${userIds.size} Twitch channels for verification! Currently there is ${twitchWebhooks.size} created webhooks!" }

			for (channelId in userIds) {
				val webhook = twitchWebhooks[channelId]

				if (webhook == null) {
					notCreatedYetChannels.add(channelId)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.debug { "${channelId}'s webhook expired! We will recreate it..." }
					twitchWebhooks.remove(channelId)
					notCreatedYetChannels.add(channelId)
				}
			}

			logger.info { "I will create ${notCreatedYetChannels.size} Twitch channel webhooks!" }

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.map { userId ->
				GlobalScope.async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						val whatApiShouldBeUsed = getWebhookRegisteringTwitchApiForUser(userId)

						// Vamos criar!
						val code = whatApiShouldBeUsed.makeTwitchApiRequest("https://api.twitch.tv/helix/webhooks/hub", "POST",
								mapOf(
										"hub.callback" to "${loritta.instanceConfig.loritta.website.url}api/v1/callbacks/pubsubhubbub?type=twitch&userid=$userId",
										"hub.lease_seconds" to "864000",
										"hub.mode" to "subscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://api.twitch.tv/helix/streams?user_id=$userId"
								))
								.status

						if (code.value != 204 && code.value != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Something went wrong while creating ${userId}'s webhook! Status Code: $code" }
							return@async null
						}

						// We need to put them outside of the .debug {} block since we *need* them to be incremented
						val incrementAndGet = webhookCount.incrementAndGet()
						logger.debug { "$userId's webhook was sucessfully created! ${userId.rem(8)} Currently there is $incrementAndGet/${webhooksToBeCreatedCount} created webhooks!" }

						return@async Pair(
								userId,
								TwitchWebhook(
										userId,
										System.currentTimeMillis(),
										864000
								)
						)
					} catch (e: Exception) {
						logger.error("Erro ao criar subscription na Twitch", e)
						null
					}
				}
			}

			runBlocking {
				tasks.forEachIndexed { index, deferred ->
					val webhook = deferred.await()

					if (webhook != null)
						twitchWebhooks[webhook.first] = webhook.second

					if (index % 50 == 0 && index != 0) { // Do not write the file if index == 0, because it would be a *very* unnecessary write
						logger.info { "Saving Twitch Webhook File... $index channels were processed" }
						twitchWebhookFile.writeText(gson.toJson(twitchWebhooks))
					}
				}

				val createdWebhooksCount = webhookCount.get()

				if (createdWebhooksCount != 0) {
					twitchWebhookFile.writeText(gson.toJson(twitchWebhooks))

					logger.info { "Successfully wrote Twitch Webhook File! ${webhookCount.get()} channels were processed" }
				} else {
					logger.info { "Successfully finished Twitch Webhook Task! No new webhooks were created..." }
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Error while processing Twitch channels" }
		}
	}
}
