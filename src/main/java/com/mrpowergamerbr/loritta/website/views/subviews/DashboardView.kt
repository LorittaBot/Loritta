package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response

class DashboardView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path == "/dashboard"
	}

	override fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val lorittaProfile = loritta.getLorittaProfileForUser(discordAuth.getUserIdentification().id)

		variables["lorittaProfile"] = lorittaProfile

		if (req.param("hideSharedServers").isSet) {
			val hideSharedServers = req.param("hideSharedServers").booleanValue()
			val hidePreviousUsernames = req.param("hidePreviousUsernames").booleanValue()

			lorittaProfile.hideSharedServers = hideSharedServers
			lorittaProfile.hidePreviousUsernames = hidePreviousUsernames

			loritta save lorittaProfile

			val response = JsonObject()
			response["api:message"] = "OK"
			response["hideSharedServers"] = hideSharedServers
			response["hidePreviousUsernames"] = hidePreviousUsernames
			return response.toString()
		}

		val guilds = discordAuth.getUserGuilds().filter {
			val guild = lorittaShards.getGuildById(it.id)
			if (guild != null) {
				val member = guild.getMemberById(lorittaProfile.userId)
				if (member != null) { // As vezes member == null, então vamos verificar se não é null antes de verificar as permissões
					val config = loritta.getServerConfigForGuild(it.id)
					val lorittaUser = GuildLorittaUser(member, config, lorittaProfile)
					LorittaWebsite.canManageGuild(it) || lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
				} else {
					LorittaWebsite.canManageGuild(it)
				}
			} else {
				LorittaWebsite.canManageGuild(it)
			}
		}

		variables["userGuilds"] = guilds
		val userPermissionLevels = mutableMapOf<TemmieDiscordAuth.DiscordGuild, LorittaWebsite.UserPermissionLevel>()
		val joinedServers = mutableMapOf<TemmieDiscordAuth.DiscordGuild, Boolean>()
		for (guild in guilds) {
			userPermissionLevels[guild] = LorittaWebsite.getUserPermissionLevel(guild)
			joinedServers[guild] = lorittaShards.getGuildById(guild.id) != null
		}
		variables["userPermissionLevels"] = userPermissionLevels
		variables["joinedServers"] = joinedServers
		return evaluate("dashboard.html", variables)
	}
}