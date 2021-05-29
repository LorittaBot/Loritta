package net.perfectdreams.loritta.website.routes.dashboard

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class DashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)

		val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
		val settings = loritta.newSuspendedTransaction { lorittaProfile.settings }
		variables["lorittaProfile"] = lorittaProfile
		variables["settings"] = settings

		val userGuilds = discordAuth.getUserGuilds()
		val serverConfigs = loritta.newSuspendedTransaction {
			ServerConfig.find { ServerConfigs.id inList userGuilds.map { it.id.toLong() } }
					.toList()
		}

		val guilds = userGuilds.filter {
			val guild = lorittaShards.getGuildById(it.id)
			if (guild != null) {
				val member = guild.retrieveMemberById(lorittaProfile.userId).await()
				val config = serverConfigs.firstOrNull { config -> config.guildId.toString() == it.id }
				if (member != null && config != null) { // As vezes member == null, então vamos verificar se não é null antes de verificar as permissões
					val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(config, member), lorittaProfile)
					LorittaWebsite.canManageGuild(it) || lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
				} else {
					LorittaWebsite.canManageGuild(it)
				}
			} else {
				LorittaWebsite.canManageGuild(it)
			}
		}

		variables["userGuilds"] = guilds
		val userPermissionLevels = mutableMapOf<TemmieDiscordAuth.Guild, LorittaWebsite.UserPermissionLevel>()
		val joinedServers = mutableMapOf<TemmieDiscordAuth.Guild, Boolean>()
		for (guild in guilds) {
			userPermissionLevels[guild] = LorittaWebsite.getUserPermissionLevel(guild)
			joinedServers[guild] = lorittaShards.getGuildById(guild.id) != null
		}
		variables["userPermissionLevels"] = userPermissionLevels
		variables["joinedServers"] = joinedServers
		variables["saveType"] = "main"

		call.respondHtml(
				evaluate("dashboard.html", variables)
		)
	}
}