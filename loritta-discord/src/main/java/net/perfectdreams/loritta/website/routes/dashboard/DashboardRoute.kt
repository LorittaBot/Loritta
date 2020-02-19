package net.perfectdreams.loritta.website.routes.dashboard

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
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
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.set

class DashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)

		val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
		val settings = transaction(Databases.loritta) { lorittaProfile.settings }
		variables["lorittaProfile"] = lorittaProfile
		variables["settings"] = settings

		val userGuilds = discordAuth.getUserGuilds()
		val mongoServerConfigs = com.mrpowergamerbr.loritta.utils.loritta.serversColl.find(Filters.`in`("_id", userGuilds.map { it.id })).toMutableList()

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
		val userPermissionLevels = mutableMapOf<TemmieDiscordAuth.Guild, LorittaWebsite.UserPermissionLevel>()
		val joinedServers = mutableMapOf<TemmieDiscordAuth.Guild, Boolean>()
		for (guild in guilds) {
			userPermissionLevels[guild] = LorittaWebsite.getUserPermissionLevel(guild)
			joinedServers[guild] = lorittaShards.getGuildById(guild.id) != null
		}
		variables["userPermissionLevels"] = userPermissionLevels
		variables["joinedServers"] = joinedServers
		call.respondHtml(
				evaluate("dashboard.html", variables)
		)
	}
}