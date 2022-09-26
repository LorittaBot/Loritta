package net.perfectdreams.loritta.legacy.website.routes.dashboard

import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.tables.GuildProfiles
import net.perfectdreams.loritta.legacy.tables.ServerConfigs
import net.perfectdreams.loritta.legacy.utils.GuildLorittaUser
import net.perfectdreams.loritta.legacy.utils.LorittaPermission
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.utils.extensions.await
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.website.LorittaWebsite
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import kotlin.collections.set

class DashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)

		val lorittaProfile = net.perfectdreams.loritta.legacy.utils.loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
		val settings = loritta.newSuspendedTransaction { lorittaProfile.settings }
		variables["lorittaProfile"] = lorittaProfile
		variables["settings"] = settings

		val userGuilds = discordAuth.getUserGuilds()
		val userGuildsIds = userGuilds.map { it.id.toLong() }

		// Update if the user is in a guild or not based on the retrieved guilds
		loritta.newSuspendedTransaction {
			GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId inList userGuildsIds) }) {
				it[GuildProfiles.isInGuild] = true
			}

			GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId notInList userGuildsIds) }) {
				it[GuildProfiles.isInGuild] = false
			}
		}

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