package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.rometools.rome.io.ParsingFeedException
import com.rometools.rome.io.SyndFeedInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.FeatureFlags
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class NewRssFeedTask : Runnable {
	companion object {
		var storedLastEntries = ConcurrentHashMap<String, MutableSet<String>>()
		var ignoreUrls = ConcurrentHashMap<String, Long>()
		private val logger = KotlinLogging.logger {}
		val coroutineDispatcher = Executors.newScheduledThreadPool(1).asCoroutineDispatcher()
	}

	override fun run() {
		if (!FeatureFlags.isEnabled("rss-feeds"))
			return

		val servers = loritta.serversColl.find(
				Filters.gt("rssFeedConfig.feeds", listOf<Any>())
		).iterator()

		// IDs das comunidades a serem verificados
		val rssFeedLinks = mutableSetOf<String>()
		val list = mutableListOf<MongoServerConfig>()

		servers.use {
			while (it.hasNext()) {
				val server = it.next()
				val rssFeedConfig = server.rssFeedConfig

				for (feed in rssFeedConfig.feeds) {
					if (feed.feedUrl != null) {
						if (ignoreUrls.contains(feed.feedUrl)) {
							val diff = System.currentTimeMillis() - ignoreUrls[feed.feedUrl!!]!!

							if (1_800_000 > diff) {
								continue
							} else {
								logger.info("A feed ${feed.feedUrl} será verificada novamente já que o cooldown de 30 minutos já terminou")
								ignoreUrls.remove(feed.feedUrl!!)
							}
						}

						rssFeedLinks.add(feed.feedUrl!!)
					}
				}

				list.add(server)
			}
		}

		logger.info("Existem ${rssFeedLinks.size} feeds que eu irei verificar! Atualmente eu conheço ${storedLastEntries.size} posts!")

		// Agora iremos verificar os canais
		val deferred = rssFeedLinks.map { rssFeedLink ->
			GlobalScope.launch(coroutineDispatcher) {
				try {
					logger.info { "Verificando link $rssFeedLink..." }
					val request = HttpRequest.get(rssFeedLink)
							.doSafeConnection()
							.connectTimeout(15000)
							.readTimeout(15000)
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
								for (serverConfig in list) {
									for (feedInfo in serverConfig.rssFeedConfig.feeds.filter { rssFeedLink  == it.feedUrl }) {
										val guild = lorittaShards.getGuildById(serverConfig.guildId) ?: continue

										val textChannel = guild.getTextChannelByNullableId(feedInfo.repostToChannelId) ?: continue

										if (!textChannel.canTalk())
											continue

										var message = feedInfo.newMessage

										if (message.isEmpty()) {
											message = "{link}"
											feedInfo.newMessage = message
											loritta save serverConfig
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

										textChannel.sendMessage(generatedMessage).queue() // Envie a mensagem
									}
								}
							}
						}
					}

					storedLastEntries[rssFeedLink] = entries.map { it.link }.toMutableSet()
				} catch (e: Exception) {
					ignoreUrls[rssFeedLink] = System.currentTimeMillis()
					logger.warn("Ignorando link $rssFeedLink devido a falha ao pegar as informações da feed")

					if (e is ParsingFeedException) // Ignorar erros de parse (de pessoas que colocam links que não são RSS feeds)
						return@launch

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