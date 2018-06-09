package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.utils.*
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class APIMixerWebhookView : NoVarsView() {
	val logger by logger()

	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/mixer-webhook"))
	}

	private fun bytesToHex(hash: ByteArray): String {
		val hexString = StringBuffer()
		for (i in hash.indices) {
			val hex = Integer.toHexString(0xff and hash[i].toInt())
			if (hex.length == 1) {
				hexString.append('0')
			}
			hexString.append(hex)
		}
		return hexString.toString()
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)

		val response = req.body().value()

		logger.info("Recebi payload do Mixer! ${response}")

		val originalSignature = req.header("Poker-Signature").value()

		val signingKey = SecretKeySpec(Loritta.config.mixerWebhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA384")
		val mac = Mac.getInstance("HmacSHA384")
		mac.init(signingKey)
		val doneFinal = mac.doFinal(response.toByteArray(Charsets.UTF_8))
		val output = "sha384=" + bytesToHex(doneFinal).toUpperCase()

		logger.info("Assinatura Original: ${originalSignature}")
		logger.info("Nossa Assinatura   : ${output}")
		logger.info("Sucesso?           : ${originalSignature == output}")

		if (originalSignature != output) {
			logger.error("Assinatura do Webhook recebido não é idêntica a nossa assinatura!!!")
			return jsonObject(
					"api:code" to LoriWebCodes.UNAUTHORIZED
			).toString()
		}

		val json = jsonParser.parse(response).obj

		val event = json["event"].string
		val payload = json["payload"].obj

		logger.info("Evento recebido: $event")

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

								var message = it.videoSentMessage ?: "{link}";

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

		return jsonObject(
				"api:code" to LoriWebCodes.SUCCESS
		).toString()
	}
}