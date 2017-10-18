package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.utils.jsonParser
import org.jooby.Request
import org.jooby.Response

class APIGetChannelInfoView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().matches(Regex("^/api/v1/get_channel_info"))
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val json = JsonObject()

		if (!req.param("channelLink").isSet) {
			json["error"] = "Missing channelLink param"
			return json.toString()
		}

		val channelLink = req.param("channelLink").value()

		val httpRequest = HttpRequest.get(channelLink)
				.header("Cookie", "YSC=g_0DTrOsgy8; PREF=f1=50000000&f6=7; VISITOR_INFO1_LIVE=r8qTZn_IpAs")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown channel"
			return json.toString()
		}

		val body = httpRequest.body()

		try {
			val youTubePayload = "window\\[\"ytInitialData\"\\] = (.+);".toPattern().matcher(body).apply { find() }

			val payload = jsonParser.parse(youTubePayload.group(1))

			val channelId = payload["header"]["c4TabbedHeaderRenderer"]["channelId"].string
			val title = payload["header"]["c4TabbedHeaderRenderer"]["title"].string
			val avatarUrl = payload["header"]["c4TabbedHeaderRenderer"]["avatar"]["thumbnails"][0]["url"].string

			json["title"] = title
			// json["description"] = description.attr("content")
			json["avatarUrl"] = avatarUrl
			json["channelId"] = channelId
			return json.toString()
		} catch (e: Exception) {
			json["raw"] = body
			return json.toString()
		}
	}
}