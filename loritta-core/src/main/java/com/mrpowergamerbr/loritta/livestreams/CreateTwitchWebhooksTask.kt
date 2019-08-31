package com.mrpowergamerbr.loritta.livestreams

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CreateTwitchWebhooksTask : Runnable {
	companion object {
		val lastNotified = Caffeine.newBuilder().expireAfterAccess(12L, TimeUnit.HOURS).build<String, Long>().asMap()
		private val logger = KotlinLogging.logger {}
	}

	var twitchWebhooks: MutableList<TwitchWebhook>? = null

	override fun run() {
		if (!loritta.isMaster) // Não verifique caso não seja o servidor mestre
			return

		try {
			// Servidores que usam o módulo do YouTube
			val servers = loritta.serversColl.find(
					Filters.gt("livestreamConfig.channels", listOf<Any>())
			)

			// User Logins dos canais a serem verificados
			val userLogins = mutableSetOf<String>()

			val list = mutableListOf<MongoServerConfig>()

			logger.info("Verificando canais da Twitch de ${servers.count()} servidores...")

			servers.iterator().use {
				while (it.hasNext()) {
					val server = it.next()
					// val guild = lorittaShards.getGuildById(server.guildId) ?: continue
					val livestreamConfig = server.livestreamConfig

					for (channel in livestreamConfig.channels) {
						if (channel.channelUrl == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						/* val textChannel = guild.getTextChannelByNullableId(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue */;

						val userLogin = channel.channelUrl!!.split("/").last()
						if (userLogin.isBlank())
							continue

						// Algumas verificações antes de adicionar
						if (!Constants.TWITCH_USERNAME_PATTERN.matcher(userLogin).matches())
							continue

						userLogins.add(userLogin)
					}
					list.add(server)
				}
			}

			// Transformar todos os nossos user logins em user IDs, para que seja usado depois
			val streamerInfos = runBlocking { loritta.twitch.getUserLogins(userLogins.toMutableList()) }

			val twitchWebhookFile = File(Loritta.FOLDER, "twitch_webhook.json")
			if (twitchWebhooks == null && twitchWebhookFile.exists()) {
				twitchWebhooks = gson.fromJson(twitchWebhookFile.readText())
			} else if (twitchWebhooks == null) {
				twitchWebhooks = mutableListOf()
			}

			val notCreatedYetChannels = mutableListOf<String>()

			logger.info("Existem ${userLogins.size} canais na Twitch que eu irei verificar! Atualmente existem ${twitchWebhooks!!.size} webhooks criadas!")

			for (userLogin in userLogins) {
				val webhook = twitchWebhooks!!.firstOrNull { it.userLogin == userLogin }

				if (webhook == null) {
					notCreatedYetChannels.add(userLogin)
					continue
				}

				if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
					logger.debug { "Webhook de $userLogin expirou! Nós iremos recriar ela..." }
					twitchWebhooks!!.remove(webhook)
					notCreatedYetChannels.add(userLogin)
				}
			}

			logger.info("Irei criar ${notCreatedYetChannels.size} webhooks para canais da Twitch!")

			val webhooksToBeCreatedCount = notCreatedYetChannels.size

			val webhookCount = AtomicInteger()

			val tasks = notCreatedYetChannels.filter { streamerInfos[it] != null }.map { userLogin ->
				GlobalScope.async(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
					try {
						val userId = streamerInfos[userLogin]?.id ?: return@async null

						// Iremos primeiro desregistrar todos os nossos testes marotos
						loritta.twitch.makeTwitchApiRequest("https://api.twitch.tv/helix/webhooks/hub", "POST",
								mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=twitch&userlogin=${userLogin.encodeToUrl()}",
										"hub.lease_seconds" to "864000",
										"hub.mode" to "unsubscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://api.twitch.tv/helix/streams?user_id=$userId"
								))
								.ok()

						// E agora realmente iremos criar!
						val code = loritta.twitch.makeTwitchApiRequest("https://api.twitch.tv/helix/webhooks/hub", "POST",
								mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=twitch&userlogin=${userLogin.encodeToUrl()}",
										"hub.lease_seconds" to "864000",
										"hub.mode" to "subscribe",
										"hub.secret" to loritta.config.mixer.webhookSecret,
										"hub.topic" to "https://api.twitch.tv/helix/streams?user_id=$userId"
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error { "Erro ao tentar criar Webhook de ${userLogin}! Código: ${code}" }
							return@async null
						}

						logger.debug { "Webhook de $userLogin criada com sucesso! Atualmente ${webhookCount.incrementAndGet()}/${webhooksToBeCreatedCount} webhooks foram criadas!" }

						return@async TwitchWebhook(
								userId,
								userLogin,
								System.currentTimeMillis(),
								864000
						)
					} catch (e: Exception) {
						logger.error("Erro ao criar subscription na Twitch", e)
						null
					}
				}
			}

			runBlocking {
				tasks.onEach {
					val webhook = it.await()

					if (webhook != null) {
						twitchWebhooks!!.add(webhook)
					}
				}

				twitchWebhookFile.writeText(gson.toJson(twitchWebhooks))
			}
		} catch (e: Exception) {
			logger.error("Erro ao processar vídeos da Twitch", e)
		}
	}
}
