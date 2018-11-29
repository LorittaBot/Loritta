package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.runBlocking
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APIGetTwitchInfoView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/get_twitch_info"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val json = JsonObject()

		if (!req.param("channelLink").isSet) {
			json["error"] = "Missing channelLink param"
			return json.toString()
		}

		val channelLink = req.param("channelLink").value()

		val userLogin = channelLink.split("/").last()
		val payload = runBlocking { loritta.twitch.getUserLogin(userLogin) }

		if (payload == null) {
			json["error"] = "Unknown channel"
			return json.toString()
		}

		return gson.toJson(payload)
	}
}