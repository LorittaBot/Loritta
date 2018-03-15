package com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.OnlineStatus
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.io.File
import java.util.*

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