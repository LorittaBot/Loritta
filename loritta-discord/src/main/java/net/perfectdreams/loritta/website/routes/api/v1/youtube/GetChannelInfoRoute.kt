package net.perfectdreams.loritta.website.routes.api.v1.youtube

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.extensions.isValidUrl
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.net.URL

class GetChannelInfoRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/youtube/channel") {
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
			} else {
				// Se for um username, temos que converter de username -> ID
				val username = urlPath.split("/").last()
				logger.info { "Getting $username's channel ID..." }
				val httpRequest = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?key=$key&forUsername=$username&part=id")

				val body = httpRequest.body()
				val jsonObject = jsonParser.parse(body).obj

				val items = jsonObject["items"].array
				if (items.size() == 0) {
					json["error"] = "Unknown Channel"
					call.respondJson(json)
					return
				}

				items.first()["id"].string
			}
			logger.info { "Checking $channelId's channel information..." }

			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails,snippet&id=$channelId&key=$key")
					.body()

			val youTubeJsonResponse = jsonParser.parse(response).obj
			val responseError = MiscUtils.getResponseError(youTubeJsonResponse)
			val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

			if (error) {
				throw RuntimeException("YouTube API key had its daily limit exceeded!")
			} else {
				val hasUploadsPlaylist = youTubeJsonResponse["items"].array[0]["contentDetails"].obj.get("relatedPlaylists").asJsonObject.has("uploads")

				json["public_uploads_playlist"] = hasUploadsPlaylist
			}

			json["title"] = youTubeJsonResponse["items"].array[0]["snippet"]["title"].string
			json["avatarUrl"] = youTubeJsonResponse["items"].array[0]["snippet"]["thumbnails"]["high"]["url"].string
			json["channelId"] = channelId
			call.respondJson(json)
			return
		} catch (e: Exception) {
			json["exception"] = e::class.qualifiedName
			logger.warn(e) { "Exception while retrieving channel information" }
			call.respondJson(json, HttpStatusCode.NotFound)
			return
		}
	}
}