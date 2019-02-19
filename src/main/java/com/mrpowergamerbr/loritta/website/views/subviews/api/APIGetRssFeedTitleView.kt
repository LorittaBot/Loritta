package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.doSafeConnection
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
				.doSafeConnection()
				.userAgent(Constants.USER_AGENT)

		val statusCode = request.code()

		if (statusCode != 200) {
			json["error"] = "Unknown channel"
			return gson.toJson(json)
		}

		val body = request.body()
		val feed = SyndFeedInput().build(body.reader())

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
