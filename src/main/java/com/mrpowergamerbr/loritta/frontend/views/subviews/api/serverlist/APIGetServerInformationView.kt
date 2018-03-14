package com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.OnlineStatus
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.io.File
import java.util.*

class APIGetServerInformationView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/information"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		val type = req.param("guildId").value()

		val server = loritta.serversColl.find(
				Filters.or(
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("serverListConfig.vanityUrl", type)
						),
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("_id", type)
						)
				)
		).firstOrNull()

		if (server == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guild = lorittaShards.getGuildById(server.guildId)!!
		val information = APIGetServerSampleView.transformToJsonObject(guild, server, userIdentification)

		return information.toString()
	}
}