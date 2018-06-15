package com.mrpowergamerbr.loritta.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import java.io.File

class CreateYouTubeWebhooksTask : Runnable {
	val logger by logger()
	var youtubeWebhooks: MutableList<YouTubeWebhook>? = null

	override fun run() {
		try {
			// Servidores que usam o módulo do YouTube
			val servers = loritta.serversColl.find(
					Filters.gt("youTubeConfig.channels", listOf<Any>())
			)

			// IDs dos canais a serem verificados
			val channelIds = mutableSetOf<String>()

			val list = mutableListOf<ServerConfig>()

			logger.info("Verificando canais do YouTube de ${servers.count()} servidores...")

			servers.iterator().use {
				while (it.hasNext()) {
					val server = it.next()
					val guild = lorittaShards.getGuildById(server.guildId) ?: continue
					val youTubeConfig = server.youTubeConfig

					for (channel in youTubeConfig.channels) {
						if (channel.channelId == null)
							continue
						if (!channel.channelUrl!!.startsWith("http"))
							continue
						val textChannel = guild.getTextChannelById(channel.repostToChannelId) ?: continue

						if (!textChannel.canTalk())
							continue

						channelIds.add(channel.channelId!!)
					}
					list.add(server)
				}
			}

			File("./youtube_channels.txt").writeText(channelIds.joinToString("\n"))

			val youtubeWebhookFile = File(Loritta.FOLDER, "youtube_webhook.json")
			if (youtubeWebhooks == null && youtubeWebhookFile.exists())
				youtubeWebhooks = gson.fromJson(youtubeWebhookFile.readText())
			else
				youtubeWebhooks = mutableListOf()

			val youtubeWebhooks = youtubeWebhooks!!

			logger.info("Existem ${channelIds.size} canais no YouTube que eu irei verificar! Atualmente existem ${youtubeWebhooks.size} webhooks criadas!")
			for (channelId in channelIds) {
				if (youtubeWebhooks.any { it.channelId == channelId }) {
					val webhook = youtubeWebhooks.first { it.channelId == channelId }

					if (System.currentTimeMillis() > webhook.createdAt + (webhook.lease * 1000)) {
						val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
								.form(mapOf(
										"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=youtubevideoupdate",
										"hub.lease_seconds" to "",
										"hub.mode" to "unsubscribe",
										"hub.secret" to Loritta.config.mixerWebhookSecret,
										"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
										"hub.verify" to "async",
										"hub.verify_token" to Loritta.config.mixerWebhookSecret
								))
								.code()

						if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
							logger.error("Erro ao tentar desregistrar Webhook de ${channelId}! Código: ${code}")
							continue
						}

						youtubeWebhooks.remove(webhook)
					} else {
						continue
					}
				}

				// Desregistrar para caso tenhamos algum subscripton aberto que a gente esqueceu
				HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
						.form(mapOf(
								"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=youtubevideoupdate",
								"hub.lease_seconds" to "",
								"hub.mode" to "unsubscribe",
								"hub.secret" to Loritta.config.mixerWebhookSecret,
								"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
								"hub.verify" to "async",
								"hub.verify_token" to Loritta.config.mixerWebhookSecret
						))
						.ok()

				val code = HttpRequest.post("https://pubsubhubbub.appspot.com/subscribe")
						.form(mapOf(
								"hub.callback" to "https://loritta.website/api/v1/callbacks/pubsubhubbub?type=youtubevideoupdate",
								"hub.lease_seconds" to "",
								"hub.mode" to "subscribe",
								"hub.secret" to Loritta.config.mixerWebhookSecret,
								"hub.topic" to "https://www.youtube.com/xml/feeds/videos.xml?channel_id=$channelId",
								"hub.verify" to "async",
								"hub.verify_token" to Loritta.config.mixerWebhookSecret
						))
						.code()

				if (code != 204 && code != 202) { // code 204 = noop, 202 = accepted (porque pelo visto o PubSubHubbub usa os dois
					logger.error("Erro ao tentar criar Webhook de ${channelId}! Código: ${code}")
					continue
				}

				youtubeWebhooks.add(
						YouTubeWebhook(
								channelId,
								System.currentTimeMillis(),
								432000
						)
				)

				youtubeWebhookFile.writeText(gson.toJson(youtubeWebhooks))
			}
		} catch (e: Exception) {
			logger.error("Erro ao processar vídeos do YouTube", e)
		}
	}
}