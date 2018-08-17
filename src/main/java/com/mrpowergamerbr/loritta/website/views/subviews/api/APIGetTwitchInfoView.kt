package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.livestreams.TwitchUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
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
		val payload = TwitchUtils.makeTwitchApiRequest("https://api.twitch.tv/helix/users?login=$userLogin").body()

		val response = jsonParser.parse(payload).obj

		val data = response["data"].array

		if (data.size() == 0) {
			json["error"] = "Unknown channel"
			return json.toString()
		}

		return data[0].toString()
	}
}