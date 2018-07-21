package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.livestreams.CreateTwitchWebhooksTask
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.bytesToHex
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Path("/api/v1/callbacks/pubsubhubbub")
class PubSubHubbubCallbackController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)
		val response = req.body().value()

		logger.info { "Recebi payload do PubSubHubbub!" }
		logger.trace { response }

		val originalSignatureHeader = req.header("X-Hub-Signature")

		if (!originalSignatureHeader.isSet) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing X-Hub-Signature Header from Request")
			res.send(payload.toString())
			return
		}

		val originalSignature = originalSignatureHeader.value()

		val output = if (originalSignature.startsWith("sha1=")) {
			val signingKey = SecretKeySpec(Loritta.config.mixerWebhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
			val mac = Mac.getInstance("HmacSHA1")
			mac.init(signingKey)
			val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
			val output = "sha1=" + doneFinal.bytesToHex()

			logger.debug { "Assinatura Original: ${originalSignature}" }
			logger.debug { "Nossa Assinatura   : ${output}" }
			logger.debug { "Sucesso?           : ${originalSignature == output}" }

			output
		} else if (originalSignature.startsWith("sha256=")) {
			val signingKey = SecretKeySpec(Loritta.config.mixerWebhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
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

		if (originalSignature != output) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Invalid X-Hub-Signature Content from Request")
			res.send(payload.toString())
			return
		}

		val typeParam = req.param("type")
		val type = typeParam.value()

		if (type == "ytvideo") {
			val payload = Jsoup.parse(response, "", Parser.xmlParser());

			val entries = payload.getElementsByTag("entry")

			val lastVideo = entries.firstOrNull() ?: return

			val videoId = lastVideo.getElementsByTag("yt:videoId").first().html()
			val lastVideoTitle = lastVideo.getElementsByTag("title").first().html()
			val published =lastVideo.getElementsByTag("published").first().html()
			val channelId =lastVideo.getElementsByTag("yt:channelId").first().html()

			val publishedEpoch = Constants.YOUTUBE_DATE_FORMAT.parse(published).time
			val storedEpoch = CreateYouTubeWebhooksTask.lastNotified[channelId]

			if (storedEpoch != null) {
				// Para evitar problemas (caso duas webhooks tenham sido criadas) e para evitar "atualizações de descrições causando updates", nós iremos verificar:
				// 1. Se o vídeo foi enviado a mais de 1 minuto do que o anterior
				// 2. Se o último vídeo foi enviado depois do último vídeo enviado
				if (System.currentTimeMillis() >= (60000 - storedEpoch) && storedEpoch >= publishedEpoch) {
					return
				}
			}

			// Vamos agora atualizar o map
			CreateYouTubeWebhooksTask.lastNotified[channelId] = publishedEpoch

			logger.info("Recebi notificação de vídeo $lastVideoTitle ($videoId) de $channelId")

			val servers = loritta.serversColl.find(
					Filters.eq("youTubeConfig.channels.channelId", channelId)
			).iterator()

			servers.use {
				while (it.hasNext()) {
					val config = it.next()

					val guild = lorittaShards.getGuildById(config.guildId) ?: continue

					val youTubeInfos = config.youTubeConfig.channels.filter { it.channelId == channelId }

					for (youTubeInfo in youTubeInfos) {
						val textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue

						var message = youTubeInfo.videoSentMessage ?: "{link}";

						if (message.isEmpty()) {
							message = "{link}"
						}

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

						textChannel.sendMessage(discordMessage).queue()
					}
				}
			}
		}

		if (type == "twitch") {
			val userLogin = req.param("userlogin").value()

			val payload = jsonParser.parse(response)
			val data = payload["data"].array

			// Se for vazio, quer dizer que é um stream down
			if (data.size() != 0) {
				for (_obj in data) {
					val obj = _obj.obj

					val gameId = obj["game_id"].string
					val title = obj["title"].string

					val storedEpoch = CreateTwitchWebhooksTask.lastNotified[userLogin]
					if (storedEpoch != null) {
						// Para evitar problemas (caso duas webhooks tenham sido criadas) e para evitar "atualizações de descrições causando updates", nós iremos verificar:
						// 1. Se o vídeo foi enviado a mais de 1 minuto do que o anterior
						// 2. Se o último vídeo foi enviado depois do último vídeo enviado
						if ((60000 + storedEpoch) >= System.currentTimeMillis()) {
							return
						}
					}

					CreateTwitchWebhooksTask.lastNotified[userLogin] = System.currentTimeMillis()

					logger.info("Recebi notificação de livestream (Twitch) $title ($gameId) de $userLogin")

					val servers = loritta.serversColl.find(
							Filters.gt("livestreamConfig.channels", listOf<Any>())
					)

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

								val storedUserLogin = channel.channelUrl!!.split("/").last()
								if (storedUserLogin == userLogin) {
									var message = channel.videoSentMessage ?: "{link}";

									if (message.isEmpty()) {
										message = "{link}"
									}

									val gameInfo = NewLivestreamThread.getGameInfo(gameId)

									val customTokens = mapOf(
											"game" to (gameInfo?.name ?: "???"),
											"title" to title,
											"link" to "https://www.twitch.tv/$userLogin"
									)

									textChannel.sendMessage(MessageUtils.generateMessage(message, null, guild, customTokens)).complete()
								}
							}
						}
					}
				}
			}
		}

		res.status(Status.NO_CONTENT)
		res.send("")
	}

	@GET
	fun request(req: Request, res: Response) {
		val hubChallengeParam = req.param("hub.challenge")

		logger.trace { "hubChallengeParam=$hubChallengeParam "}

		if (!hubChallengeParam.isSet) {
			logger.error { "Recebi um request para ativar uma subscription, mas o request não possuia o hub.challenge!" }
			res.status(Status.NOT_FOUND)
			res.send("")
			return
		}

		// Já que a Twitch não suporta verify tokens, nós apenas iremos ignorar os tokens de verificação
		val hubChallenge = hubChallengeParam.value()

		res.status(Status.OK)
		res.send(hubChallenge)
	}
}