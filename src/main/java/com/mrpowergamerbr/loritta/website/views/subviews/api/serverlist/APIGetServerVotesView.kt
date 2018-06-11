package com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.utils.*
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APIGetServerVotesView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/get-server-votes"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val type = req.param("guildId").value()

		val server = loritta.serversColl.find(
				Filters.eq("_id", type)
		).firstOrNull()

		if (server == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val header = req.header("LoriGuild-Authentication")
		val auth = header.value("???")

		if (server.apiKey != auth) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val json = GSON.toJsonTree(server.serverListConfig.votes).array

		json.forEach {
			it.obj.remove("ip")
			it.obj.remove("email")
		}

		return json.toString()
	}
}