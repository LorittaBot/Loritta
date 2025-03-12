package net.perfectdreams.loritta.morenitta.website.routes.api.v1.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.isValidUrl
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedYouTubeChannelIds
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jsoup.Jsoup
import java.net.URL

class GetChannelInfoRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/youtube/channel") {
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

		if (!loritta.connectionManager.isTrusted(channelLink)) {
			json["error"] = "Untrusted URL"
			call.respondJson(json)
			return
		}

		val urlPath = URL(channelLink).path

		val key = loritta.config.loritta.youtube.key

		try {
			// Paths que começam com "/channel/" significa que já é um channel ID,
			// já que começam com "/user/" significa que é um username
			// Exemplos:
			// https://www.youtube.com/user/TeamMojang
			// https://www.youtube.com/channel/UCZ-uXTZGSN8lmp-nrXwz7-A
			val channelId = if (urlPath.startsWith("/channel/")) {
				urlPath.removePrefix("/channel/")
			} else if (urlPath.startsWith("/c/") || urlPath.startsWith("/@")) {
				// Channels starting with "/c/" is harder because the API doesn't show them (for some reason...)
				// To retrieve those, we need to query the channel URL and get the ID from there
				// We also directly connect to the channel page if the URL starts with "/@", to handle new YouTube handles (example: https://www.youtube.com/@MrPowerGamerBR)
				val youTubePage = Jsoup.connect(channelLink)
					// Follow any redirects
					.followRedirects(true)
					// If we use Firefox's user agent, YouTube throws us a page with a 304 redirect, to avoid this, we will use curl's user agent
					// Which, for some reason... doesn't have that?
					.userAgent("curl/7.64.1")
					.get()

				youTubePage.select("[property='og:url']")
					.first()!!
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
				CachedYouTubeChannelIds.selectAll().where {
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
					loritta,
					LoriWebCode.ITEM_NOT_FOUND,
					"Exception while retrieving channel information"
				)
			)
		}
	}
}