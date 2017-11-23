package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import org.jooby.Request
import org.jooby.Response

class APIGetTwitchInfoView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().matches(Regex("^/api/v1/get_twitch_info"))
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val json = JsonObject()

		if (!req.param("channelLink").isSet) {
			json["error"] = "Missing channelLink param"
			return json.toString()
		}

		val channelLink = req.param("channelLink").value()

		val userLogin = channelLink.split("/").last()
		val payload = HttpRequest.get("https://api.twitch.tv/helix/users?login=$userLogin")
				.header("Client-ID", Loritta.config.twitchClientId)
				.body()

		val response = JSON_PARSER.parse(payload).obj

		val data = response["data"].array

		if (data.size() == 0) {
			json["error"] = "Unknown channel"
			return json.toString()
		}

		return data[0].toString()
	}
}