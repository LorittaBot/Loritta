package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.bytesToHex
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Path("/api/v1/callbacks/mixer")
class MixerCallbackController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val response = req.body().value()

		logger.info { "Recebi payload do Mixer!" }
		logger.trace { response }

		val originalSignatureHeader = req.header("Poker-Signature")

		if (!originalSignatureHeader.isSet) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Missing Poker-Signature Header from Request")
			res.send(payload.toString())
			return
		}

		val originalSignature = originalSignatureHeader.value()

		val signingKey = SecretKeySpec(loritta.config.mixer.webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA384")
		val mac = Mac.getInstance("HmacSHA384")
		mac.init(signingKey)
		val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
		val output = "sha384=" + doneFinal.bytesToHex().toUpperCase()

		logger.debug { "Assinatura Original: ${originalSignature}" }
		logger.debug { "Nossa Assinatura   : ${output}" }
		logger.debug { "Sucesso?           : ${originalSignature == output}" }

		if (originalSignature != output) {
			res.status(Status.UNAUTHORIZED)
			val payload = WebsiteUtils.createErrorPayload(LoriWebCode.UNAUTHORIZED, "Invalid Poker-Signature Content from Request")
			res.send(payload.toString())
			return
		}

		val json = jsonParser.parse(response).obj

		val event = json["event"].string
		val payload = json["payload"].obj

		logger.debug { "Evento recebido: $event" }

		val channelId = event.split(":")[1]
		val onlineStatus = payload["online"].nullBool

		if (onlineStatus != null) {
			if (onlineStatus && !NewLivestreamThread.isMixerLivestreaming.contains(channelId)) {
				NewLivestreamThread.isMixerLivestreaming.add(channelId)
				logger.info("${channelId} está ao vivo! Vamos anunciar para todos os interessados...")

				// Anunciar para quem for necessário
				val servers = loritta.serversColl.find(
						Filters.gt("livestreamConfig.channels", listOf<Any>())
				).toMutableList()

				val channelNamePattern = Regex("mixer\\.com\\/([A-z0-9]+)").toPattern()

				for (server in servers) {
					val guild = lorittaShards.getGuildById(server.guildId) ?: continue

					val livestreamConfig = server.livestreamConfig

					val mixerChannels = livestreamConfig.channels.filter { it.channelUrl?.startsWith("https://mixer.com/") ?: false || it.channelUrl?.startsWith("http://mixer.com/") ?: false }

					// Canais do Mixer permitem que a gente atualize uma webhook com vários eventos (yay, mágica!)
					// Ou seja, caso um novo canal seja adicionado, é melhor a gente deletar a webhook atual e criar uma nova (woosh, mágica!)
					for (it in mixerChannels) {
						val channelUrl = it.channelUrl ?: continue

						val matcher = channelNamePattern.matcher(channelUrl)

						if (matcher.find()) {
							val channelName = matcher.group(1)

							// Agora nós iremos fazer um request para pegar o ID do canal, caso seja necessário
							val serverChannelId = NewLivestreamThread.mixerUsernameToId.getOrPut(channelName, {
								// Okay, nós não sabemos quem é esse cara... daora a vida...
								val payload = HttpRequest.get("https://mixer.com/api/v1/channels/$channelName?fields=id")
										.acceptJson()
										.body()

								val channelId = jsonParser.parse(payload).obj["id"].nullLong

								if (channelId != null) {
									NewLivestreamThread.logger.info("ID do canal de ${channelName} é ${channelId}!")
									channelId
								} else {
									-1
								}
							})

							if (serverChannelId.toString() == channelId) {
								// Yay, este servidor conhece o nosso parça!
								val repostToChannelId = it.repostToChannelId ?: continue

								val textChannel = guild.getTextChannelById(repostToChannelId) ?: continue

								if (!textChannel.canTalk())
									continue

								var message = it.videoSentMessage ?: "{link}"

								if (message.isEmpty()) {
									message = "{link}"
								}

								val customTokens = mapOf(
										"link" to it.channelUrl!!
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
			} else {
				NewLivestreamThread.isMixerLivestreaming.remove(channelId)
			}
		}

		res.status(Status.NO_CONTENT)
		res.send("")
	}
}