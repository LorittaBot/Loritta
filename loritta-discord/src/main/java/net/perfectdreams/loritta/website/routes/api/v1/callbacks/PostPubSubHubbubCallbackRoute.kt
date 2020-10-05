package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.*
import com.google.common.cache.CacheBuilder
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.livestreams.CreateTwitchWebhooksTask
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.bytesToHex
import com.mrpowergamerbr.loritta.utils.extensions.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.userAgent
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.request.receiveStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SentYouTubeVideoIds
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.urlQueryString
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PostPubSubHubbubCallbackRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/callbacks/pubsubhubbub") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val streamingSince = CacheBuilder.newBuilder()
				.expireAfterAccess(4, TimeUnit.HOURS)
				.build<Long, Long>()
				.asMap()
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val response = call.receiveStream().bufferedReader(charset = Charsets.UTF_8).readText()

		logger.info { "Recebi payload do PubSubHubbub!" }
		logger.trace { response }

		val originalSignature = call.request.header("X-Hub-Signature")
				?: throw WebsiteAPIException(
						HttpStatusCode.Unauthorized,
						WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing X-Hub-Signature Header from Request")
				)

		val output = if (originalSignature.startsWith("sha1=")) {
			val signingKey = SecretKeySpec(com.mrpowergamerbr.loritta.utils.loritta.config.mixer.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
			val mac = Mac.getInstance("HmacSHA1")
			mac.init(signingKey)
			val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
			val output = "sha1=" + doneFinal.bytesToHex()

			logger.debug { "Assinatura Original: ${originalSignature}" }
			logger.debug { "Nossa Assinatura   : ${output}" }
			logger.debug { "Sucesso?           : ${originalSignature == output}" }

			output
		} else if (originalSignature.startsWith("sha256=")) {
			val signingKey = SecretKeySpec(com.mrpowergamerbr.loritta.utils.loritta.config.mixer.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
			val mac = Mac.getInstance("HmacSHA256")
			mac.init(signingKey)
			val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
			val output = "sha256=" + doneFinal.bytesToHex()

			logger.debug { "Assinatura Original: ${originalSignature}" }
			logger.debug { "Nossa Assinatura   : ${output}" }
			logger.debug { "Sucesso?           : ${originalSignature == output}" }

			output
		} else {
			throw NotImplementedError("${originalSignature} is not implemented yet!")
		}

		if (originalSignature != output)
			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Invalid X-Hub-Signature Header from Request")
			)

		val type = call.parameters["type"]

		if (type == "ytvideo") {
			val payload = Jsoup.parse(response, "", Parser.xmlParser())

			val entries = payload.getElementsByTag("entry")

			val lastVideo = entries.firstOrNull() ?: return

			val videoId = lastVideo.getElementsByTag("yt:videoId").first().html()
			val lastVideoTitle = lastVideo.getElementsByTag("title").first().html()
			val published = lastVideo.getElementsByTag("published").first().html()
			val channelId = lastVideo.getElementsByTag("yt:channelId").first().html()

			val publishedEpoch = Constants.YOUTUBE_DATE_FORMAT.parse(published).time

			if (com.mrpowergamerbr.loritta.utils.loritta.isMaster) {
				val wasAlreadySent = loritta.newSuspendedTransaction {
					SentYouTubeVideoIds.select {
						SentYouTubeVideoIds.channelId eq channelId and (SentYouTubeVideoIds.videoId eq videoId)
					}.count() != 0L
				}

				if (!wasAlreadySent) {
					loritta.newSuspendedTransaction {
						SentYouTubeVideoIds.insert {
							it[SentYouTubeVideoIds.videoId] = videoId
							it[SentYouTubeVideoIds.channelId] = channelId
							it[receivedAt] = System.currentTimeMillis()
						}
					}
					relayPubSubHubbubNotificationToOtherClusters(call, originalSignature, response)
				} else {
					logger.warn { "Video $lastVideoTitle ($videoId) from $channelId was already sent, so... bye!" }
					return
				}
			}

			logger.info("Recebi notificação de vídeo $lastVideoTitle ($videoId) de $channelId")

			val trackedAccounts = loritta.newSuspendedTransaction {
				TrackedYouTubeAccounts.select {
					TrackedYouTubeAccounts.youTubeChannelId eq channelId
				}.toList()
			}

			val guildIds = mutableListOf<Long>()
			val canTalkGuildIds = mutableListOf<Long>()

			for (trackedAccount in trackedAccounts) {
				guildIds.add(trackedAccount[TrackedYouTubeAccounts.guildId])

				val guild = lorittaShards.getGuildById(trackedAccount[TrackedYouTubeAccounts.guildId]) ?: continue

				val textChannel = guild.getTextChannelById(trackedAccount[TrackedYouTubeAccounts.channelId]) ?: continue

				if (!textChannel.canTalk())
					continue

				var message = trackedAccount[TrackedYouTubeAccounts.message]

				if (message.isEmpty())
					message = "{link}"

				val customTokens = mapOf(
						"título" to lastVideoTitle,
						"title" to lastVideoTitle,
						"link" to "https://youtu.be/$videoId",
						"video-id" to videoId
				)

				val discordMessage = MessageUtils.generateMessage(
						message,
						listOf(guild),
						guild,
						customTokens
				) ?: continue

				textChannel.sendMessage(discordMessage)
						.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing(canTalkGuildIds.size)

				canTalkGuildIds.add(trackedAccount[TrackedYouTubeAccounts.guildId])
			}

			// Nós iremos fazer relay de todos os vídeos para o servidor da Lori
			val textChannel = lorittaShards.getTextChannelById(Constants.RELAY_YOUTUBE_VIDEOS_CHANNEL)

			textChannel?.sendMessage("""${lastVideoTitle.escapeMentions()} — https://youtu.be/$videoId
						|**Enviado em...**
						|${guildIds.joinToString("\n", transform = { "`$it`" })}
					""".trimMargin())?.queue()
		}

		if (type == "twitch") {
			if (com.mrpowergamerbr.loritta.utils.loritta.isMaster)
				relayPubSubHubbubNotificationToOtherClusters(call, originalSignature, response)

			val userId = call.parameters["userid"]!!.toLong()

			val payload = JsonParser.parseString(response)
			val data = payload["data"].array

			val guildIds = mutableListOf<Long>()
			val canTalkGuildIds = mutableListOf<Long>()

			// Se for vazio, quer dizer que é um stream down
			if (data.size() != 0) {
				for (_obj in data) {
					if (streamingSince.containsKey(userId))
						continue

					streamingSince[userId] = System.currentTimeMillis()

					val obj = _obj.obj

					val gameId = obj["game_id"].string
					val title = obj["title"].string

					val storedEpoch = CreateTwitchWebhooksTask.lastNotified[userId]
					if (storedEpoch != null) {
						// Para evitar problemas (caso duas webhooks tenham sido criadas) e para evitar "atualizações de descrições causando updates", nós iremos verificar:
						// 1. Se o vídeo foi enviado a mais de 1 minuto do que o anterior
						// 2. Se o último vídeo foi enviado depois do último vídeo enviado
						if ((60000 + storedEpoch) >= System.currentTimeMillis()) {
							return
						}
					}

					CreateTwitchWebhooksTask.lastNotified[userId] = System.currentTimeMillis()

					val accountInfo = loritta.twitch.getUserLoginById(userId)
					if (accountInfo == null) {
						logger.info { "Received livestream (Twitch) notification $title ($gameId) for $userId, but I can't find the user" }
					} else {
						logger.info { "Received livestream notification (Twitch) $title ($gameId) of ${accountInfo.id} ($userId)" }

						val trackedAccounts = loritta.newSuspendedTransaction {
							TrackedTwitchAccounts.select {
								TrackedTwitchAccounts.twitchUserId eq userId
							}.toList()
						}

						for (trackedAccount in trackedAccounts) {
							guildIds.add(trackedAccount[TrackedTwitchAccounts.guildId])

							val guild = lorittaShards.getGuildById(trackedAccount[TrackedTwitchAccounts.guildId])
									?: continue

							val textChannel = guild.getTextChannelById(trackedAccount[TrackedTwitchAccounts.channelId])
									?: continue

							if (!textChannel.canTalk())
								continue

							var message = trackedAccount[TrackedTwitchAccounts.message]

							if (message.isEmpty()) {
								message = "{link}"
							}

							val gameInfo = com.mrpowergamerbr.loritta.utils.loritta.twitch.getGameInfo(gameId)

							val customTokens = mapOf(
									"game" to (gameInfo?.name ?: "???"),
									"title" to title,
									"link" to "https://www.twitch.tv/${accountInfo.login}"
							)

							val discordMessage = MessageUtils.generateMessage(
									message,
									listOf(guild),
									guild,
									customTokens
							) ?: continue

							textChannel.sendMessage(discordMessage)
									.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing(canTalkGuildIds.size)

							canTalkGuildIds.add(trackedAccount[TrackedYouTubeAccounts.guildId])
						}

						// Nós iremos fazer relay de todos os vídeos para o servidor da Lori
						val textChannel = lorittaShards.getTextChannelById(Constants.RELAY_TWITCH_STREAMS_CHANNEL)

						textChannel?.sendMessage("""${title.escapeMentions()} — https://www.twitch.tv/${accountInfo.login}
									|**Enviado em...**
									|${guildIds.joinToString("\n", transform = { "`$it`" })}
								""".trimMargin())?.queue()
					}
				}
			} else {
				// Stream down, streamer parou de streamar
				streamingSince.remove(userId)
			}
		}
		call.respondJson(jsonObject())
	}

	fun relayPubSubHubbubNotificationToOtherClusters(call: ApplicationCall, originalSignature: String, response: String) {
		logger.info { "Relaying PubSubHubbub request to other instances, because I'm the master server! :3" }

		val shards = com.mrpowergamerbr.loritta.utils.loritta.config.clusters.filter { it.id != 1L }

		shards.map {
			GlobalScope.launch {
				try {
					withTimeout(25_000) {
						logger.info { "Sending request to ${"https://${it.getUrl()}${call.request.path()}${call.request.urlQueryString}"}..." }
						loritta.http.post<HttpResponse>("https://${it.getUrl()}${call.request.path()}${call.request.urlQueryString}") {
							userAgent(com.mrpowergamerbr.loritta.utils.loritta.lorittaCluster.getUserAgent())
							header("X-Hub-Signature", originalSignature)

							body = response
						}
					}
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw ClusterOfflineException(it.id, it.name)
				}
			}
		}
	}
}