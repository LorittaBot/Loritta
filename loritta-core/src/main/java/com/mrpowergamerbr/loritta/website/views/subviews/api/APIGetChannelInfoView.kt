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
			val youTubePayload = "window\\[\"ytInitialData\"\\] = (.+);".toPattern().matcher(body).apply { find() }

			val payload = jsonParser.parse(youTubePayload.group(1))

			val channelId = payload["header"]["c4TabbedHeaderRenderer"]["channelId"].string
			val title = payload["header"]["c4TabbedHeaderRenderer"]["title"].string
			val avatarUrl = payload["header"]["c4TabbedHeaderRenderer"]["avatar"]["thumbnails"][0]["url"].string

			val key = loritta.youtubeKey

			var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=$channelId&key=$key")
					.body()

			var json = jsonParser.parse(response).obj
			val responseError = getResponseError(json)
			val error = responseError == "dailyLimitExceeded" || responseError == "quotaExceeded"

			if (error) {
				println("[!] Removendo key $key...")
				loritta.youtubeKeys.remove(key)
			} else {
				var hasUploadsPlaylist = json["items"].array[0]["contentDetails"].obj.get("relatedPlaylists").asJsonObject.has("uploads")

				json["public_uploads_playlist"] = hasUploadsPlaylist
			}

			json["title"] = title
			// json["description"] = description.attr("content")
			json["avatarUrl"] = avatarUrl
			json["channelId"] = channelId
			return gson.toJson(json)
		} catch (e: Exception) {
			json["raw"] = body
			return gson.toJson(json)
		}
	}
}