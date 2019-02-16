package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.gson
import com.rometools.rome.io.SyndFeedInput
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APIGetRssFeedTitleView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/get_feed_title"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val json = JsonObject()

		if (!req.param("feedLink").isSet) {
			json["error"] = "Missing channelLink param"
			return gson.toJson(json)
		}

		val channelLink = req.param("feedLink").value()

		val request = HttpRequest.get(channelLink)
				.userAgent(Constants.USER_AGENT)

		val statusCode = request.code()

		if (statusCode != 200) {
			json["error"] = "Unknown channel"
			return gson.toJson(json)
		}

		val body = request.body()
		val feed = SyndFeedInput().build(body.reader())

		val httpRequest = HttpRequest.get(channelLink)
				.header("Cookie", "YSC=g_0DTrOsgy8; PREF=f1=50000000&f6=7; VISITOR_INFO1_LIVE=r8qTZn_IpAs")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown channel"
			return gson.toJson(json)
		}

		try {
			json["title"] = feed.title
			json["entryTitle"] = feed.entries.firstOrNull()?.title ?: "???"
			return gson.toJson(json)
		} catch (e: Exception) {
			json["error"] = e.cause.toString()
			return gson.toJson(json)
		}
	}
}
