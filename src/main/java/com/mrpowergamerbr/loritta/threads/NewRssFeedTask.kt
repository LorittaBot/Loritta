package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta.Companion.config
import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.rometools.rome.io.SyndFeedInput
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class NewRssFeedTask : Runnable {
	companion object {
		var storedLastEntries = ConcurrentHashMap<String, MutableSet<String>>()
		val logger = LoggerFactory.getLogger(AminoRepostTask::class.java)
	}

	override fun run() {
		val servers = loritta.serversColl.find(
				Filters.gt("rssFeedConfig.feeds", listOf<Any>())
		).iterator()

		// IDs das comunidades a serem verificados
		val rssFeedLinks = mutableSetOf<String>()
		val list = mutableListOf<ServerConfig>()

		servers.use {
			while (it.hasNext()) {
				val server = it.next()
				val rssFeedConfig = server.rssFeedConfig

				for (feed in rssFeedConfig.feeds) {
					if (feed.feedUrl != null)
						rssFeedLinks.add(feed.feedUrl!!)
				}

				list.add(server)
			}
		}

		logger.info("Existem ${rssFeedLinks.size} feeds que eu irei verificar! Atualmente eu conheço ${storedLastEntries.size} posts!")

		// Agora iremos verificar os canais
		val deferred = rssFeedLinks.map { rssFeedLink ->
			launch(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
				try {
					logger.info("Verificando link $rssFeedLink...")
					val request = HttpRequest.get(rssFeedLink)
							.userAgent(Constants.USER_AGENT)

					val statusCode = request.code()

					if (statusCode != 200) {
						logger.error("Erro ao verificar feed $rssFeedLink, status code: $statusCode")
						return@launch
					}

					val body = request.body()
					val feed = SyndFeedInput().build(body.reader())

					val entries = feed.entries.reversed()
					if (storedLastEntries[rssFeedLink] != null) {
						val storedEntriesLink = storedLastEntries[rssFeedLink]!!
						for (entry in entries) {
							if (!storedEntriesLink.contains(entry.link)) {
								// avisar a todos que desejam receber a novidade
								for (server in list) {
									for (feedInfo in server.rssFeedConfig.feeds.filter { rssFeedLink  == it.feedUrl }) {
										val guild = lorittaShards.getGuildById(server.guildId) ?: continue

										val textChannel = guild.getTextChannelById(feedInfo.repostToChannelId) ?: continue

										if (!textChannel.canTalk())
											continue

										var message = feedInfo.newMessage

										if (message.isEmpty()) {
											message = "{link}"
											feedInfo.newMessage = message
											loritta save config
										}

										val customTokens = mutableMapOf<String, String>()
										if (entry.description != null) {
											customTokens["descrição"] = entry.description.value
											customTokens["description"] = entry.description.value
										}

										customTokens["título"] = entry.title
										customTokens["title"] = entry.title
										customTokens["link"] = entry.link

										val generatedMessage = MessageUtils.generateMessage(message, null, guild, customTokens) ?: continue

										textChannel.sendMessage(generatedMessage).complete() // Envie a mensagem
									}
								}
							}
						}
					}

					storedLastEntries[rssFeedLink] = entries.map { it.link }.toMutableSet()
				} catch (e: Exception) {
					logger.error(rssFeedLink, e)
				}
			}
		}

		runBlocking {
			deferred.onEach {
				it.join()
			}
		}
	}
}