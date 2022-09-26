package net.perfectdreams.loritta.morenitta.website.routes.api.v1

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.lorittaShards
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.urlQueryString
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresAPIGuildAuthRoute(loritta: LorittaBot, originalDashboardPath: String) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/guilds/{guildId}$originalDashboardPath") {
	abstract suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig)

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val guildId = call.parameters["guildId"] ?: return

		val shardId = DiscordUtils.getShardIdFromGuildId(guildId.toLong())

		val host = call.request.hostFromHeader()

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loriShardId)

		if (host != theNewUrl)
			redirect("https://$theNewUrl${call.request.path()}${call.request.urlQueryString}", false)

		val jdaGuild = lorittaShards.getGuildById(guildId)
				?: throw WebsiteAPIException(
						HttpStatusCode.BadRequest,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNKNOWN_GUILD,
								"Guild $guildId doesn't exist or it isn't loaded yet"
						)
				)

		val serverConfig = net.perfectdreams.loritta.morenitta.utils.loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		val id = userIdentification.id
		val member = jdaGuild.retrieveMemberById(id).await()
		var canAccessDashboardViaPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(serverConfig, member), net.perfectdreams.loritta.morenitta.utils.loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
		}

		val canBypass = net.perfectdreams.loritta.morenitta.utils.loritta.config.isOwner(userIdentification.id) || canAccessDashboardViaPermission
		if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true || jdaGuild.ownerId == userIdentification.id)) {
			throw WebsiteAPIException(
					HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.FORBIDDEN,
							"User ${member?.user?.id} doesn't have permission to edit ${guildId}'s config"
					)
			)
		}

		return onGuildAuthenticatedRequest(call, discordAuth, userIdentification, jdaGuild, serverConfig)
	}
}