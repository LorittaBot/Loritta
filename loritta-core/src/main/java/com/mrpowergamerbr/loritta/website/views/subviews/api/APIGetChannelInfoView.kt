package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.MiscUtils.getResponseError
import com.mrpowergamerbr.loritta.utils.extensions.isValidUrl
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup

class APIGetChannelInfoView : NoVarsView() {
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

		val httpRequest = HttpRequest.get(channelLink)
				.header("Cookie", "YSC=g_0DTrOsgy8; PREF=f1=50000000&f6=7; VISITOR_INFO1_LIVE=r8qTZn_IpAs")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown channel"
			return gson.toJson(json)
		}

		val body = httpRequest.body()

		try {
			val jsoup = Jsoup.parse(body)

			val canonicalLink = jsoup.getElementsByTag("link").firstOrNull { it.attr("rel") == "canonical" }?.attr("href") ?: run {
				json["error"] = "Canonical Link is missing!"
				return gson.toJson(json)
			}

			val channelId = canonicalLink.split("/").last()

			val key = loritta.youtubeKey

			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails,snippet&id=$channelId&key=$key")
					.body()

			val youTubeJsonResponse = jsonParser.parse(response).obj
			val responseError = getResponseError(youTubeJsonResponse)
			val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

			if (error) {
				println("[!] Removendo key $key...")
				loritta.youtubeKeys.remove(key)
			} else {
				val hasUploadsPlaylist = youTubeJsonResponse["items"].array[0]["contentDetails"].obj.get("relatedPlaylists").asJsonObject.has("uploads")

				json["public_uploads_playlist"] = hasUploadsPlaylist
			}

			json["title"] = youTubeJsonResponse["items"].array[0]["snippet"]["title"].string
			json["avatarUrl"] = youTubeJsonResponse["items"].array[0]["snippet"]["thumbnails"]["high"]["url"].string
			json["channelId"] = channelId
			return gson.toJson(json)
		} catch (e: Exception) {
			json["raw"] = body
			return gson.toJson(json)
		}
	}
}