package net.perfectdreams.loritta.watchdog

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import mu.KotlinLogging
import net.perfectdreams.loritta.watchdog.WatchdogBot.Companion.jsonParser
import org.jsoup.Jsoup
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder

class DiscordPingStatusTracker(val bot: WatchdogBot) {
	companion object {
		val logger = KotlinLogging.logger {}
		val API_RESPONSE_METRIC_ID = "bjtwhflp2322"
	}

	var lastLatencyResult = -1
	var lastBroadcastedPing = -1
	var lastPublishedStatus: SyndEntry? = null
	val twitter by lazy {
		val tf = TwitterFactory(buildTwitterConfig())
		tf.instance
	}

	suspend fun checkStatus() {
		val discordPing = getPingLatency()

		val rawDifference = discordPing - lastBroadcastedPing
		val difference = Math.abs(rawDifference)

		logger.info { "Current API latency is ${discordPing}ms, difference between then and now is ${difference}ms (raw: $rawDifference})" }

		if (lastLatencyResult != -1) {
			if (difference >= 50 && (discordPing >= 250 || 0 > rawDifference)) {
				logger.info { "Tweeting about the API latency change!" }
				// broadcast
				lastBroadcastedPing = discordPing

				if (bot.config.discordStatusCheck.tweet) {
					val tweet = if (0 >= rawDifference) {
						// latency down
						"\uD83D\uDE0A↘️ Discord's REST API latency is decreasing!"
					} else {
						// latency up
						"\uD83D\uDE2D↗️ Discord's REST API latency is increasing... You may encounter issues when sending messages, connecting to Discord and when trying to use bots!"
					} + "\n\n\uD83D\uDCE1 Ping: ${discordPing}ms"

					val status = StatusUpdate(tweet)

					twitter.updateStatus(status)
				}
			}
		}

		lastLatencyResult = discordPing
		if (lastBroadcastedPing == -1)
			lastBroadcastedPing = lastLatencyResult

		val lastestEntry = getDiscordStatus()
		logger.info { "Latest official discord status is ${lastestEntry.title} ${lastestEntry.description} at ${lastestEntry.publishedDate}" }

		val previousPublishedStatus = lastPublishedStatus
		this.lastPublishedStatus = lastestEntry

		if (previousPublishedStatus != null && lastestEntry.publishedDate != previousPublishedStatus.publishedDate) {
			logger.info { "Tweeting about the discord official status change!" }

			// oof, time to update!
			val title = lastestEntry.title
			val description = lastestEntry.description.value

			val jsoup = Jsoup.parse(description)

			val firstParagraph = jsoup.getElementsByTag("p").first()

			val type = firstParagraph.getElementsByTag("strong").text()
			val typeDescription = firstParagraph.ownText()

			val tweet = "\uD83D\uDCE3 Discord Status Update\n\n\uD83D\uDCF0 $title • $type\n\n$typeDescription".substringIfNeeded(range = 0 until 200) + " " + lastestEntry.link

			if (bot.config.discordStatusCheck.tweet) {
				val status = StatusUpdate(tweet)

				twitter.updateStatus(status)
			}
		}
	}

	suspend fun getPingLatency(): Int {
		val pingPayload = bot.http.get<HttpResponse>("https://discord.statuspage.io/metrics-display/ztt4777v23lf/day.json")
				.readText()

		val payload = JsonParser.parseString(pingPayload)

		val metrics = payload["metrics"].array

		val apiResponseMetric = metrics.firstOrNull {
			it["metric"]["id"].string == API_RESPONSE_METRIC_ID
		} ?: throw RuntimeException("API Response Metric not found!")

		val data = apiResponseMetric["data"].array.last()

		val latency = data["value"].int

		return latency
	}

	suspend fun getDiscordStatus(): SyndEntry {
		val statusRssPayload = bot.http.get<HttpResponse>("https://discord.statuspage.io/history.rss")
				.readText()

		val feed = SyndFeedInput().build(statusRssPayload.reader())

		val entries = feed.entries.sortedByDescending {
			it.publishedDate
		}

		val lastEntry = entries.first()

		return lastEntry
	}

	private fun buildTwitterConfig(): Configuration {
		val cb = ConfigurationBuilder()
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(bot.config.twitter.oAuthConsumerKey)
				.setOAuthConsumerSecret(bot.config.twitter.oAuthConsumerSecret)
				.setOAuthAccessToken(bot.config.twitter.oAuthAccessToken)
				.setOAuthAccessTokenSecret(bot.config.twitter.oAuthAccessTokenSecret)

		return cb.build()
	}

	fun String.substringIfNeeded(range: IntRange = 0 until 2000, suffix: String = "..."): String {
		if (this.isEmpty()) {
			return this
		}

		if (this.length - 1 in range)
			return this

		return this.substring(range.start .. range.last - suffix.length) + suffix
	}
}