package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response

class DashboardView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path() == "/dashboard"
	}

	override fun renderProtected(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val guilds = discordAuth.getUserGuilds().filter { LorittaWebsite.canManageGuild(it) }

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