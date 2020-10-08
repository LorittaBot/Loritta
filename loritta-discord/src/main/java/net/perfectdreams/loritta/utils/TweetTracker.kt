package net.perfectdreams.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LorittaShards
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import twitter4j.*
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import kotlin.concurrent.thread

class TweetTracker(val m: Loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val streams = mutableListOf<TwitterStream>()
	var restartStream = false

	fun buildTwitterConfig(): Configuration {
		val cb = ConfigurationBuilder()
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(m.config.twitter.oAuthConsumerKey)
				.setOAuthConsumerSecret(m.config.twitter.oAuthConsumerSecret)
				.setOAuthAccessToken(m.config.twitter.oAuthAccessToken)
				.setOAuthAccessTokenSecret(m.config.twitter.oAuthAccessTokenSecret)

		return cb.build()
	}

	fun updateStreams() {
		startStreams()
	}

	fun startStreams() {
		logger.info { "Starting twitter streams..." }
		val followAccounts = transaction(Databases.loritta) {
			TrackedTwitterAccounts.selectAll().map { it[TrackedTwitterAccounts.twitterAccountId] }.distinct()
		}

		val chunks = followAccounts.chunked(5_000) // 400 keywords, 5,000 userids and 25 location boxes
		logger.info { "There will be ${chunks.size} twitter streams..." }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			while (true) {
				delay(300_000 * Math.max(streams.size.toLong(), 1L)) // O delay varia dependendo de quantas streams existem

				if (restartStream) { // Caso tenha que reiniciar
					restartStream = false
					updateStreams() // Reiniciar todas as streams (Mas isto irá recriar essa task!)
					return@launch // E por isso apenas iremos cancelar a task e deixar a nova continuar a recriar as streams
				}
			}
		}

		if (streams.size > chunks.size) {
			// There are more streams than it should have! Let's shut down all exceeding streams
			val toBeRemovedStreams = streams.drop(chunks.size)
			toBeRemovedStreams.forEach { it.clearListeners(); it.cleanUp() }
			streams.removeAll(toBeRemovedStreams)
		}

		for ((index, window) in chunks.withIndex()) {
			thread {
				val twitterStream = streams.getOrNull(index) ?: run {
					val stream = TwitterStreamFactory(buildTwitterConfig()).instance
					streams.add(stream)
					stream
				}

				twitterStream.clearListeners()
				
				twitterStream.addListener(object : StatusListener {
					override fun onTrackLimitationNotice(p0: Int) {
						logger.warn(p0.toString())
					}

					override fun onStallWarning(p0: StallWarning) {
						logger.warn(p0.toString())
					}

					override fun onException(p0: Exception) {
						logger.warn(p0) { "" }
					}

					override fun onDeletionNotice(p0: StatusDeletionNotice) {}

					override fun onStatus(p0: Status) {
						if (p0.isRetweet) // É um retweet
							return

						if (p0.user.id !in window) // ID do usuário não está na window... Então para que fazer relay?
							return

						if (twitterStream !in streams) // Stream *seems* to be removed, since it isn't on the stream list anymore
							return

						logger.info { "Received status ${p0.id} from ${p0.user.screenName} (${p0.user.id}), relaying to other clusters..." }

						val payload = jsonObject(
								"tweetId" to p0.id,
								"userId" to p0.user.id,
								"screenName" to p0.user.screenName
						)

						val shards = loritta.config.clusters

						shards.map {
							GlobalScope.async(loritta.coroutineDispatcher) {
								try {
									val body = HttpRequest.post("https://${it.getUrl()}/api/v1/twitter/received-tweet")
											.userAgent(loritta.lorittaCluster.getUserAgent())
											.header("Authorization", loritta.lorittaInternalApiKey.name)
											.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
											.readTimeout(loritta.config.loritta.clusterReadTimeout)
											.send(
													gson.toJson(
															payload
													)
											)
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

					override fun onScrubGeo(p0: Long, p1: Long) {}
				})

				val tweetFilterQuery = FilterQuery()
				tweetFilterQuery.follow(
						*(window.toLongArray()) // OR on keywords
				)

				logger.info { "Starting Twitter Tweet Tracker... Using query $tweetFilterQuery" }

				twitterStream.filter(tweetFilterQuery)
			}
		}
	}
}