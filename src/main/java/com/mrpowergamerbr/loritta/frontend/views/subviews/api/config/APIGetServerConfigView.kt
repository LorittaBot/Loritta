package com.mrpowergamerbr.loritta.frontend.views.subviews.api.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.Permission
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APIGetServerConfigView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/config/get-server-config"))
	}

	override fun render(req: Request, res: Response, path: String): String {
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

		if (userIdentification == null) { // Unauthozied (Discord)
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		if (!req.param("guildId").isSet) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guildId = req.param("guildId").value() // get guild id
		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val id = userIdentification.id
		val member = server.getMemberById(id)

		if (member == null) { // not in server
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return payload.toString()
		}

		val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))
		var canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

		var canOpen = id == Loritta.config.ownerId || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

		if (!canOpen) { // not authorized (perm side)
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val textChannels = JsonArray()
		for (textChannel in server.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name

			textChannels.add(json)
		}

		serverConfigJson["textChannels"] = textChannels

		val roles = JsonArray()
		for (role in server.roles) {
			val json = JsonObject()

			json["id"] = role.id
			json["name"] = role.name
			json["isPublicRole"] = role.isPublicRole
			json["isManaged"] = role.isManaged
			json["canInteract"] = server.selfMember.canInteract(role)

			roles.add(json)
		}

		serverConfigJson["roles"] = roles

		return serverConfigJson.toString()
	}
}