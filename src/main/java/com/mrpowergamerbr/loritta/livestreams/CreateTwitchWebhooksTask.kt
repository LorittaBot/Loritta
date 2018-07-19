package com.mrpowergamerbr.loritta.livestreams

import com.github.salomonbrys.kotson.fromJson
import com.google.common.flogger.FluentLogger
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class CreateTwitchWebhooksTask : Runnable {
	companion object {
		private val logger = FluentLogger.forEnclosingClass()
	}

	var twitchWebhooks: MutableList<TwitchWebhook>? = null

	override fun run() {
		try {
			// Servidores que usam o módulo do YouTube
			val servers = loritta.serversColl.find(
					Filters.gt("livestreamConfig.channels", listOf<Any>())
			)

			// User Logins dos canais a serem verificados
			val userLogins = mutableSetOf<String>()

			val list = mutableListOf<ServerConfig>()

			logger.atInfo().log("Verificando canais da Twitch de %s servidores...", servers.count())

			servers.iterator().use {
				while (it.hasNext()) {
					val server = it.next()
					val guild = lorittaShards.getGuildById(server.guildId) ?: continue
					val livestreamConfig = server.livestreamConfig

					for (channel in livestreamConfig.channels) {
						if (channel.channelUrl == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						val textChannel = guild.getTextChannelById(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue

						val userLogin = channel.channelUrl!!.split("/").last()
						if (userLogin.isBlank())
							continue

						userLogins.add(userLogin)
					}
					list.add(server)
				}
			}

			// Transformar todos os nossos user logins em user IDs, para que seja usado depois
			TwitchUtils.queryUserLogins(userLogins.toMutableList())

			val twitchWebhookFile = File(Loritta.FOLDER, "twitch_webhook.json")
			if (twitchWebhooks == null && twitchWebhookFile.exists()) {
				twitchWebhooks = gson.fromJson(twitchWebhookFile.readText())
			} else if (twitchWebhooks == null) {
				twitchWebhooks = mutableListOf()
			}

			val notCreatedYetChannels = mutableListOf<String>()

			logger.atInfo().log("Existem %s canais na Twitch que eu irei verificar! Atualmente existem %s webhooks criadas!", userLogins.size, twitchWebhooks!!.size)

			for (userLogin in userLogins) {
				val webhook = twitchWebhooks!!.firstOrNull { it.userLogin == userLogin }

				if (webhook == null) {
					notCreatedYetChannels.add(userLogin)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.atFine().log("Webhook de %s expirou! Nós iremos recriar ela...", userLogin)
					twitchWebhooks!!.remove(webhook)
					notCreatedYetChannels.add(userLogin)
				}
			}

			logger.atFine().log("Irei criar ${notCreatedYetChannels.size} webhooks para canais da Twitch!", notCreatedYetChannels.size)

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.filter { TwitchUtils.userLogin2Id[it] != null }.map { userLogin ->
				async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						val userId = TwitchUtils.userLogin2Id[userLogin]!!

						// Iremos primeiro desregistrar todos os nossos testes marotos
						TwitchUtils.makeTwitchApiRequestSuspend("https://api.twitch.tv/helix/webhooks/hub", "POST",
								mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=twitch&userlogin=${userLogin.encodeToUrl()}",
										"hub.lease_seconds" to "864000",
										"hub.mode" to "unsubscribe",
										"hub.secret" to Loritta.config.mixerWebhookSecret,
										"hub.topic" to "https://api.twitch.tv/helix/streams?user_id=$userId"
								))
								.ok()

						// E agora realmente iremos criar!
						val request = TwitchUtils.makeTwitchApiRequestSuspend("https://api.twitch.tv/helix/webhooks/hub", "POST",
								mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=twitch&userlogin=${userLogin.encodeToUrl()}",
										"hub.lease_seconds" to "864000",
										"hub.mode" to "subscribe",
										"hub.secret" to Loritta.config.mixerWebhookSecret,
										"hub.topic" to "https://api.twitch.tv/helix/streams?user_id=$userId"
								))

						val code = request.code()
						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.atSevere().log("Erro ao tentar criar Webhook de %s! Código: %s - %s", userLogin, code, request.body())
							return@async null
						}

						logger.atFine().log("Webhook de %s criada com sucesso! Atualmente %s/%s webhooks foram criadas!", webhookCount.incrementAndGet(), webhooksToBeCreatedCount)
						return@async TwitchWebhook(
								userId,
								userLogin,
								System.currentTimeMillis(),
								864000
						)
					} catch (e: Exception) {
						logger.atSevere().withCause(e).log("Erro ao criar subscription na Twitch")
						null
					}
				}
			}

			runBlocking(loritta.coroutineDispatcher) {
				tasks.onEach {
					val webhook = it.await()

					if (webhook != null) {
						twitchWebhooks!!.add(webhook)
					}
				}

				twitchWebhookFile.writeText(gson.toJson(twitchWebhooks))
			}
		} catch (e: Exception) {
			logger.atSevere().withCause(e).log("Erro ao processar vídeos da Twitch")
		}
	}
}