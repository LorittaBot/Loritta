package net.perfectdreams.loritta.website.routes.dashboard

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.hostFromHeader
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.redirect
import net.perfectdreams.loritta.website.utils.extensions.urlQueryString
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
				?: redirect(com.mrpowergamerbr.loritta.utils.loritta.discordInstanceConfig.discord.addBotUrl + "&guild_id=$guildId", false)

		logger.info { "JDA Guild get and check: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val serverConfig = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		logger.info { "Getting legacy config: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val id = userIdentification.id
		val member = jdaGuild.retrieveMemberOrNullById(id)
		var canAccessDashboardViaPermission = false

		logger.info { "OG Perm Check: ${System.currentTimeMillis() - start}" }

		if (member != null) {
			start = System.currentTimeMillis()
			val lorittaUser = GuildLorittaUser(member, LorittaUser.loadMemberLorittaPermissions(serverConfig, member), com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
			logger.info { "Lori User Perm Check: ${System.currentTimeMillis() - start}" }
		}

		val canBypass = com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userIdentification.id) || canAccessDashboardViaPermission
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