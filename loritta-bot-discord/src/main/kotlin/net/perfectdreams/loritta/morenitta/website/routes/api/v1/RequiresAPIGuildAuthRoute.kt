package net.perfectdreams.loritta.morenitta.website.routes.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.urlQueryString
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresAPIGuildAuthRoute(loritta: LorittaBot, originalDashboardPath: String) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/guilds/{guildId}$originalDashboardPath") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	abstract suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig)

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val guildId = call.parameters["guildId"] ?: return

		val shardId = DiscordUtils.getShardIdFromGuildId(loritta, guildId.toLong())

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(loritta, shardId)

		if (loriShardId != loritta.clusterId) {
			val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loritta, loriShardId)
			redirect("$theNewUrl${call.request.path()}${call.request.urlQueryString}", false)
		}

		val id = userIdentification.id
		val cacheKey = "$guildId#${id}"
		val isCachedMissingHit = loritta.cachedFailedMemberQueryResults.containsKey(cacheKey)
		if (isCachedMissingHit)
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.FORBIDDEN,
					"User $id doesn't have permission to edit ${guildId}'s config"
				)
			)

		val jdaGuild = loritta.lorittaShards.getGuildById(guildId)
			?: throw WebsiteAPIException(
				HttpStatusCode.BadRequest,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.UNKNOWN_GUILD,
					"Guild $guildId doesn't exist or it isn't loaded yet"
				)
			)

		val isGuildBanned = LorittaUtils.isGuildBanned(loritta, jdaGuild)
		if (isGuildBanned) {
			logger.info { "User ${userIdentification.id} in server ${jdaGuild.idLong} attempted to use guild API, but it is banned!" }
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.FORBIDDEN,
					"Server ${jdaGuild.idLong} is banned!"
				)
			)
		}

		val serverConfig = loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		val member = jdaGuild.retrieveMemberOrNullById(id)
		var canAccessDashboardViaPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(loritta, member, LorittaUser.loadMemberLorittaPermissions(loritta, serverConfig, member), loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
		}

		val canBypass = loritta.isOwner(userIdentification.id) || canAccessDashboardViaPermission
		if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true || jdaGuild.ownerId == userIdentification.id)) {
			loritta.cachedFailedMemberQueryResults[cacheKey] = true
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.FORBIDDEN,
					"User ${member?.user?.id} doesn't have permission to edit ${guildId}'s config"
				)
			)
		}

		return onGuildAuthenticatedRequest(call, discordAuth, userIdentification, jdaGuild, serverConfig)
	}
}