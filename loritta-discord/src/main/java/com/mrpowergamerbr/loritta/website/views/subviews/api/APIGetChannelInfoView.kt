package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.MiscUtils.getResponseError
import com.mrpowergamerbr.loritta.utils.extensions.isValidUrl
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.net.URL

class APIGetChannelInfoView : NoVarsView() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/get_channel_info"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val json = JsonObject()

		if (!req.param("channelLink").isSet) {
			json["error"] = "Missing channelLink param"
			return gson.toJson(json)
		}

		val channelLink = req.param("channelLink").value()

		if (!channelLink.isValidUrl()) {
			json["error"] = "Invalid URL"
			return json.toString()
		}

		if (!loritta.connectionManager.isTrusted(channelLink)) {
			json["error"] = "Untrusted URL"
			return json.toString()
		}

		val urlPath = URL(channelLink).path

		val key = loritta.config.youtube.apiKey

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
					return gson.toJson(json)
				}

				items.first()["id"].string
			}
			logger.info { "Checking $channelId's channel information..." }

			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails,snippet&id=$channelId&key=$key")
					.body()

			val youTubeJsonResponse = jsonParser.parse(response).obj
			val responseError = getResponseError(youTubeJsonResponse)
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
			return gson.toJson(json)
		} catch (e: Exception) {
			json["exception"] = e::class.qualifiedName
			logger.warn(e) { "Exception while retrieving channel information" }
			return gson.toJson(json)
		}
	}
}