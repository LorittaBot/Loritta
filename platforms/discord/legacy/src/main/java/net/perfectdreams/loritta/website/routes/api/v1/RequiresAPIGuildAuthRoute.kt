package net.perfectdreams.loritta.website.routes.api.v1

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.website.utils.extensions.redirect
import net.perfectdreams.loritta.website.utils.extensions.urlQueryString
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresAPIGuildAuthRoute(loritta: LorittaDiscord, originalDashboardPath: String) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/guilds/{guildId}$originalDashboardPath") {
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

		val serverConfig = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		val id = userIdentification.id
		val member = jdaGuild.retrieveMemberById(id).await()
		var canAccessDashboardViaPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(serverConfig, member), com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
		}

		val canBypass = com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userIdentification.id) || canAccessDashboardViaPermission
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