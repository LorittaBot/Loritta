package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.rometools.rome.io.ParsingFeedException
import com.rometools.rome.io.SyndFeedInput
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.DefaultRssFeeds
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedRssFeeds
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class NewRssFeedTask : Runnable {
	companion object {
		var storedLastEntries = ConcurrentHashMap<String, MutableSet<String>>()
		var ignoreUrls = ConcurrentHashMap<String, Long>()
		private val logger = KotlinLogging.logger {}
		val coroutineDispatcher = Executors.newScheduledThreadPool(8).asCoroutineDispatcher()
	}

	override fun run() {
		if (!FeatureFlags.isEnabled("rss-feeds"))
			return

		val defaultFeeds = transaction(Databases.loritta) {
			DefaultRssFeeds.selectAll()
					.toMutableList()
		}

		val feeds = transaction(Databases.loritta) {
			val allFeeds = TrackedRssFeeds.selectAll()
					.toMutableList()

			val donationOnlyFeeds = mutableListOf<ResultRow>()

			for (feed in allFeeds) {
				val serverConfig = loritta.getOrCreateServerConfig(feed[TrackedRssFeeds.guildId])
				val donationKeyValues = serverConfig.getActiveDonationKeysValue()

				if (LorittaPrices.CUSTOM_RSS_FEEDS > donationKeyValues) {
					// Usuário não é um doador! Será que nós devemos verificar essa feed?
					val feedUrl = feed[TrackedRssFeeds.feedUrl]

					if (feedUrl.startsWith("{") && feedUrl.endsWith("}")) // É um que precisar ser remapped
						donationOnlyFeeds.add(feed)
				} else {
					// Usuário é um doador!
					donationOnlyFeeds.add(feed)
				}
			}

			donationOnlyFeeds
		}

		// Todos os links que deverão ser verificados, mas iremos ignorar repetidos já que não serão necessários
		val linksToBeChecked = feeds.map { it[TrackedRssFeeds.feedUrl] }.distinct()

		val deferred = linksToBeChecked.map { rssFeedLink ->
			GlobalScope.launch(coroutineDispatcher) {
				try {
					logger.info { "Verifying RSS Feed Link $rssFeedLink..." }

					var realLink = rssFeedLink
					val remapRss = defaultFeeds.firstOrNull { "{${it[DefaultRssFeeds.feedId]}}" == realLink }

					if (remapRss != null)
						realLink = remapRss[DefaultRssFeeds.feedUrl]

					val request = HttpRequest.get(realLink)
							.connectTimeout(15_000)
							.readTimeout(15_000)
							.userAgent(Constants.USER_AGENT)

					val statusCode = request.code()

					if (statusCode != 200) {
						logger.error("Error while verifying feed $rssFeedLink, status code: $statusCode")
						return@launch
					}

					val body = request.body()
					val feed = SyndFeedInput().build(body.reader())

					val entries = feed.entries

					logger.info { "Feed $realLink has ${entries.size} entries!" }

					if (storedLastEntries[realLink] != null) { // Se já existe entries relacionadas a esse link...
						val storedEntriesLink = storedLastEntries[realLink]!!

						for (entry in entries) {
							if (!storedEntriesLink.contains(entry.link)) {
								// Avisar a todas as guilds que desejam receber a novidade
								logger.info { "New entry in $rssFeedLink feed! $entry" }

								// Repassar para todos os outros clusters!
								val shards = loritta.config.clusters

								val feedPayload = gson.toJson(
										jsonObject(
												"feedUrl" to rssFeedLink,
												"entry" to jsonObject(
														"title" to entry.title,
														"link" to entry.link
												)
										)
								)

								shards.map {
									GlobalScope.async {
										try {
											val body = HttpRequest.post("https://${it.getUrl()}/api/v1/rss/received-entry")
													.userAgent(loritta.lorittaCluster.getUserAgent())
													.header("Authorization", loritta.lorittaInternalApiKey.name)
													.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
													.readTimeout(loritta.config.loritta.clusterReadTimeout)
													.send(feedPayload)
													.body()

											JsonParser.parseString(
													body
											)
										} catch (e: Exception) {
											LorittaShards.logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
											throw ClusterOfflineException(it.id, it.name)
										}
									}
								}
							}
						}
					}

					// Guardar os últimos links enviados
					storedLastEntries[realLink] = entries.map { it.link }.toMutableSet()
				} catch (e: Exception) {
					ignoreUrls[rssFeedLink] = System.currentTimeMillis()
					logger.warn("Ignorning link $rssFeedLink due to feed failure")

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