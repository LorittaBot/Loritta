package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.bytesToHex
import com.mrpowergamerbr.loritta.website.LoriWebCode
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
	val logger by logger()

	@POST
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)
		val response = req.body().value()

		logger.info("Recebi payload do PubSubHubbub! ${response}")

		val originalSignatureHeader = req.header("X-Hub-Signature")

		if (!originalSignatureHeader.isSet) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing X-Hub-Signature Header from Request")
			res.send(payload.toString())
			return
		}

		val originalSignature = originalSignatureHeader.value()

		val signingKey = SecretKeySpec(Loritta.config.mixerWebhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
		val mac = Mac.getInstance("HmacSHA1")
		mac.init(signingKey)
		val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
		val output = "sha1=" + doneFinal.bytesToHex()

		logger.info("Assinatura Original: ${originalSignature}")
		logger.info("Nossa Assinatura   : ${output}")
		logger.info("Sucesso?           : ${originalSignature == output}")

		if (originalSignature != output) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Invalid X-Hub-Signature Content from Request")
			res.send(payload.toString())
			return
		}

		val typeParam = req.param("type")
		val type = typeParam.value()

		if (type == "youtubevideoupdate") {
			val payload = Jsoup.parse(response, "", Parser.xmlParser());

			val entries = payload.getElementsByTag("entry")

			val lastVideo = entries.firstOrNull() ?: return

			val videoId = lastVideo.getElementsByTag("yt:videoId").first().html()
			val lastVideoTitle = lastVideo.getElementsByTag("title").first().html()
			val published =lastVideo.getElementsByTag("published").first().html()
			val channelId =lastVideo.getElementsByTag("yt:channelId").first().html()

			SocketServer.logger.info("Recebi notificação de vídeo $lastVideoTitle ($videoId) de $channelId")

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
		res.status(Status.NO_CONTENT)
		res.send("")
	}

	@GET
	fun request(req: Request, res: Response) {
		logger.info("Recebi um request para ativar subscription de um PubSubHubbub!")

		val hubChallengeParam = req.param("hub.challenge")

		if (!hubChallengeParam.isSet) {
			logger.error("Recebi um request para ativar uma subscription, mas o request não possuia o hub.challenge!")
			res.status(Status.NOT_FOUND)
			res.send("")
			return
		}

		val hubVerifyTokenParam = req.param("hub.verify_token")

		if (!hubVerifyTokenParam.isSet) {
			logger.error("Recebi um request para ativar uma subscription, mas o request não possuia o hub.verify_token!")
			res.status(Status.NOT_FOUND)
			res.send("")
		}

		val hubVerifyToken = hubVerifyTokenParam.value()

		if (hubVerifyToken != Loritta.config.mixerWebhookSecret) {
			logger.error("Recebi um request para ativar uma subscription, mas o request não possui o nosso hub.verify_token! Token recebido: ${hubVerifyToken}")
			res.status(Status.NOT_FOUND)
			res.send("")
		}

		val hubChallenge = hubChallengeParam.value()

		res.status(Status.OK)
		res.send(hubChallenge)
	}
}