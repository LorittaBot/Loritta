package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response

class DashboardView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path == "/dashboard"
	}

	override fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").get()
		val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
		val settings = transaction(Databases.loritta) { lorittaProfile.settings }
		variables["lorittaProfile"] = lorittaProfile
		variables["settings"] = settings

		if (req.param("hideSharedServers").isSet) {
			val hideSharedServers = req.param("hideSharedServers").booleanValue()
			val hidePreviousUsernames = req.param("hidePreviousUsernames").booleanValue()

			transaction(Databases.loritta) {
				settings.hideSharedServers = hideSharedServers
				settings.hidePreviousUsernames = hidePreviousUsernames
			}

			val response = JsonObject()
			response["api:message"] = "OK"
			response["hideSharedServers"] = hideSharedServers
			response["hidePreviousUsernames"] = hidePreviousUsernames
			return response.toString()
		}

		val userGuilds = discordAuth.getUserGuilds()
		val mongoServerConfigs = loritta.serversColl.find(Filters.`in`("_id", userGuilds.map { it.id })).toMutableList()

		val guilds = userGuilds.filter {
			val guild = lorittaShards.getGuildById(it.id)
			if (guild != null) {
				val member = guild.getMemberById(lorittaProfile.userId)
				val config = mongoServerConfigs.firstOrNull { config -> config.guildId == it.id }
				if (member != null && config != null) { // As vezes member == null, então vamos verificar se não é null antes de verificar as permissões
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