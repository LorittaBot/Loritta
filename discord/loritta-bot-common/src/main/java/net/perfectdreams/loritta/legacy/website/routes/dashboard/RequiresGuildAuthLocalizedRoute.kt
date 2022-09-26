package net.perfectdreams.loritta.legacy.website.routes.dashboard

import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.utils.GuildLorittaUser
import net.perfectdreams.loritta.legacy.utils.LorittaPermission
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.website.LorittaWebsite
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.utils.DiscordUtils
import net.perfectdreams.loritta.legacy.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.redirect
import net.perfectdreams.loritta.legacy.website.utils.extensions.urlQueryString
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresGuildAuthLocalizedRoute(loritta: LorittaDiscord, originalDashboardPath: String) : RequiresDiscordLoginLocalizedRoute(loritta, "/guild/{guildId}$originalDashboardPath") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	abstract suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig)

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		var start = System.currentTimeMillis()
		val guildId = call.parameters["guildId"] ?: return

		val shardId = DiscordUtils.getShardIdFromGuildId(guildId.toLong())

		val host = call.request.hostFromHeader()
		val scheme = LorittaWebsite.WEBSITE_URL.split(":").first()

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loriShardId)

		logger.info { "Getting some stuff lol: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		if (host != theNewUrl)
			redirect("$scheme://$theNewUrl${call.request.path()}${call.request.urlQueryString}", false)

		val jdaGuild = lorittaShards.getGuildById(guildId)
				?: redirect(net.perfectdreams.loritta.legacy.utils.loritta.discordInstanceConfig.discord.addBotUrl + "&guild_id=$guildId", false)

		logger.info { "JDA Guild get and check: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val serverConfig = net.perfectdreams.loritta.legacy.utils.loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		logger.info { "Getting legacy config: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val id = userIdentification.id
		val member = jdaGuild.retrieveMemberOrNullById(id)
		var canAccessDashboardViaPermission = false

		logger.info { "OG Perm Check: ${System.currentTimeMillis() - start}" }

		if (member != null) {
			start = System.currentTimeMillis()
			val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(serverConfig, member), net.perfectdreams.loritta.legacy.utils.loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
			logger.info { "Lori User Perm Check: ${System.currentTimeMillis() - start}" }
		}

		val canBypass = net.perfectdreams.loritta.legacy.utils.loritta.config.isOwner(userIdentification.id) || canAccessDashboardViaPermission
		if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true || jdaGuild.ownerId == userIdentification.id)) {
			call.respondText("Você não tem permissão!")
			return
		}

		// variables["serverConfig"] = legacyServerConfig
		// TODO: Remover isto quando for removido o "server-config-json" do website
		start = System.currentTimeMillis()
		val variables = call.legacyVariables(locale)
		logger.info { "Legacy Vars Creation: ${System.currentTimeMillis() - start}" }
		variables["guild"] = jdaGuild

		return onGuildAuthenticatedRequest(call, locale, discordAuth, userIdentification, jdaGuild, serverConfig)
	}
}