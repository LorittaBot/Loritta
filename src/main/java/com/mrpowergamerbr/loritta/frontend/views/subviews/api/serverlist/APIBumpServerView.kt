package com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Role
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.json.XML
import java.net.InetAddress
import java.util.*

class APIBumpServerView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/bump"))
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

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val type = req.param("guildId").value()

		val serverConfig = loritta.serversColl.find(
				Filters.eq("_id", type)
		).firstOrNull()

		if (serverConfig == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guild = lorittaShards.getGuildById(type)!!
		if (guild.getMemberById(userIdentification.id) == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return payload.toString()
		}

		if (serverConfig.serverListConfig.lastBump + 14_400_000 > System.currentTimeMillis()) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.RATE_LIMITED
			return payload.toString()
		}

		val profile = loritta.getLorittaProfileForUser(userIdentification.id)

		if (750 > profile.dreams) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INSUFFICIENT_FUNDS
			return payload.toString()
		}

		profile.dreams -= 750

		serverConfig.serverListConfig.lastBump = System.currentTimeMillis()
		loritta save serverConfig
		loritta save profile

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS
		payload["bumpedAt"] = serverConfig.serverListConfig.lastBump

		Loritta.logger.info("${userIdentification.id} promoveu ${serverConfig.guildId}!")

		return payload.toString()
	}
}