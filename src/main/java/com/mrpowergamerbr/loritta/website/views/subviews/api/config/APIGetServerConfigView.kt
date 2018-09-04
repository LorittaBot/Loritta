package com.mrpowergamerbr.loritta.website.views.subviews.api.config

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import net.dv8tion.jda.core.Permission
import org.jooby.Request
import org.jooby.Response

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
		var canAccessDashboardViaPermission = false
		var hasAdministrationOrManageServerPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
			hasAdministrationOrManageServerPermission = member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)
		}

		val canBypass = id == Loritta.config.ownerId || canAccessDashboardViaPermission

		if (!canBypass && !hasAdministrationOrManageServerPermission) {
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

		val voteArray = serverConfigJson["serverListConfig"]["votes"].array
		val newArray = JsonArray()
		voteArray.forEach {
			it["ip"] = null
			it["email"] = null
		}
		serverConfigJson["serverListConfig"]["votes"] = newArray

		return serverConfigJson.toString()
	}
}