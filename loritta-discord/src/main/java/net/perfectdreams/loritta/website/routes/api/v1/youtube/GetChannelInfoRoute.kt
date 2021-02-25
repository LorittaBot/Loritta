package net.perfectdreams.loritta.website.routes.api.v1.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.isValidUrl
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.CachedYouTubeChannelIds
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jsoup.Jsoup
import java.net.URL

class GetChannelInfoRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/youtube/channel") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val json = JsonObject()

		val channelLink = call.parameters["channelLink"]

		if (channelLink == null) {
			json["error"] = "Missing channelLink param"
			call.respondJson(json)
			return
		}

		if (!channelLink.isValidUrl()) {
			json["error"] = "Invalid URL"
			call.respondJson(json)
			return
		}

		if (!com.mrpowergamerbr.loritta.utils.loritta.connectionManager.isTrusted(channelLink)) {
			json["error"] = "Untrusted URL"
			call.respondJson(json)
			return
		}

		val urlPath = URL(channelLink).path

		val key = com.mrpowergamerbr.loritta.utils.loritta.config.youtube.apiKey

		try {
			// Paths que começam com "/channel/" significa que já é um channel ID,
			// já que começam com "/user/" significa que é um username
			// Exemplos:
			// https://www.youtube.com/user/TeamMojang
			// https://www.youtube.com/channel/UCZ-uXTZGSN8lmp-nrXwz7-A
			val channelId = if (urlPath.startsWith("/channel/")) {
				urlPath.removePrefix("/channel/")
			} else if (urlPath.startsWith("/c/")) {
				// Channels starting with "/c/" is harder because the API doesn't show them (for some reason...)
				// To retrieve those, we need to query the channel URL and get the ID from there
				val youTubePage = Jsoup.connect(channelLink)
						.userAgent(Constants.USER_AGENT)
						.get()

				youTubePage.select("[property='og:url']")
						.first()
						.attr("content")
						.substringAfter("/channel/")
			} else {
				// Se for um username, temos que converter de username -> ID
				val username = urlPath.split("/").last()
				logger.info { "Getting $username's channel ID from the username..." }
				val httpRequest = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?key=$key&forUsername=$username&part=id")

				val body = httpRequest.body()
				val jsonObject = JsonParser.parseString(body).obj

				val items = jsonObject["items"].array
				if (items.size() == 0) {
					json["error"] = "Unknown Channel"
					call.respondJson(json)
					return
				}

				items.first()["id"].string
			}
			logger.info { "Checking if $channelId's channel information is cached..." }

			val cachedChannelInformation = loritta.newSuspendedTransaction {
				// Remover do cache, caso tenha
				CachedYouTubeChannelIds.deleteWhere {
					CachedYouTubeChannelIds.channelId eq channelId and (CachedYouTubeChannelIds.retrievedAt lessEq System.currentTimeMillis() - Constants.ONE_WEEK_IN_MILLISECONDS)
				}
				// E agora pegar o canal!
				CachedYouTubeChannelIds.select {
					CachedYouTubeChannelIds.channelId eq channelId
				}.firstOrNull()
			}

			if (cachedChannelInformation != null) {
				logger.info { "$channelId's channel information is cached! Let's use the cache info then :3" }
				val title = cachedChannelInformation[CachedYouTubeChannelIds.title]
				val avatarUrl = cachedChannelInformation[CachedYouTubeChannelIds.avatarUrl]

				json["title"] = title
				json["avatarUrl"] = avatarUrl
				json["channelId"] = channelId
				call.respondJson(json)
				return
			}

			logger.info { "Checking $channelId's channel information..." }

			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=$channelId&key=$key")
					.body()

			val youTubeJsonResponse = JsonParser.parseString(response).obj
			val responseError = MiscUtils.getResponseError(youTubeJsonResponse)
			val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

			if (error)
				throw RuntimeException("YouTube API key had its daily limit exceeded!")

			val title = youTubeJsonResponse["items"].array[0]["snippet"]["title"].string
			val avatarUrl = youTubeJsonResponse["items"].array[0]["snippet"]["thumbnails"]["high"]["url"].string

			json["title"] = title
			json["avatarUrl"] = avatarUrl
			json["channelId"] = channelId

			// Cache
			loritta.newSuspendedTransaction {
				CachedYouTubeChannelIds.insert {
					it[CachedYouTubeChannelIds.channelId] = channelId
					it[CachedYouTubeChannelIds.avatarUrl] = avatarUrl
					it[CachedYouTubeChannelIds.title] = title
					it[retrievedAt] = System.currentTimeMillis()
				}
			}

			call.respondJson(json)
			return
		} catch (e: Exception) {
			json["exception"] = e::class.qualifiedName
			logger.warn(e) { "Exception while retrieving channel information" }
			throw WebsiteAPIException(
					HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND,
							"Exception while retrieving channel information"
					)
			)
		}
	}
}