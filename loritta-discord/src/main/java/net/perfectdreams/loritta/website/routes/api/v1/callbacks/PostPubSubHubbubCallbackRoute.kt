package net.perfectdreams.loritta.website.routes.api.v1.callbacks

import com.github.salomonbrys.kotson.jsonObject
import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.extensions.bytesToHex
import com.mrpowergamerbr.loritta.utils.extensions.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SentYouTubeVideoIds
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.urlQueryString
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PostPubSubHubbubCallbackRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/callbacks/pubsubhubbub") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val streamingSince = CacheBuilder.newBuilder()
				.expireAfterAccess(4, TimeUnit.HOURS)
				.build<Long, Long>()
				.asMap()
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val response = withContext(Dispatchers.IO) {
			call.receiveStream().bufferedReader(charset = Charsets.UTF_8).readText()
		}

		logger.info { "Recebi payload do PubSubHubbub!" }
		logger.trace { response }

		val originalSignature = call.request.header("X-Hub-Signature")
				?: throw WebsiteAPIException(
						HttpStatusCode.Unauthorized,
						WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing X-Hub-Signature Header from Request")
				)

		val output = if (originalSignature.startsWith("sha1=")) {
			val signingKey = SecretKeySpec(com.mrpowergamerbr.loritta.utils.loritta.config.generalWebhook.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
			val mac = Mac.getInstance("HmacSHA1")
			mac.init(signingKey)
			val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
			val output = "sha1=" + doneFinal.bytesToHex()

			logger.debug { "Assinatura Original: ${originalSignature}" }
			logger.debug { "Nossa Assinatura   : ${output}" }
			logger.debug { "Sucesso?           : ${originalSignature == output}" }

			output
		} else if (originalSignature.startsWith("sha256=")) {
			val signingKey = SecretKeySpec(com.mrpowergamerbr.loritta.utils.loritta.config.generalWebhook.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
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