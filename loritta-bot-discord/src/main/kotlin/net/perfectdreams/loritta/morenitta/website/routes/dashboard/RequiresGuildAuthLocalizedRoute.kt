package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.urlQueryString
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

abstract class RequiresGuildAuthLocalizedRoute(loritta: LorittaBot, originalDashboardPath: String) : RequiresDiscordLoginLocalizedRoute(loritta, "/guild/{guildId}$originalDashboardPath") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	abstract suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig)

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		var start = System.currentTimeMillis()
		val guildId = call.parameters["guildId"] ?: return

		val shardId = DiscordUtils.getShardIdFromGuildId(loritta, guildId.toLong())

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(loritta, shardId)

		logger.info { "Getting some stuff lol: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		if (loriShardId != loritta.clusterId) {
			val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loritta, loriShardId)
			redirect("$theNewUrl${call.request.path()}${call.request.urlQueryString}", false)
		}

		val jdaGuild = loritta.lorittaShards.getGuildById(guildId)
				?: redirect(LorittaDiscordOAuth2AddBotURL(loritta, guildId.toLong()).toString(), false)

		logger.info { "JDA Guild get and check: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val serverConfig = loritta.getOrCreateServerConfig(guildId.toLong()) // get server config for guild

		logger.info { "Getting legacy config: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val id = userIdentification.id
		val member = jdaGuild.retrieveMemberOrNullById(id)
		var canAccessDashboardViaPermission = false

		logger.info { "OG Perm Check: ${System.currentTimeMillis() - start}" }

		if (member != null) {
			start = System.currentTimeMillis()
			val lorittaUser = GuildLorittaUser(loritta, member, LorittaUser.loadMemberLorittaPermissions(loritta, serverConfig, member), loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
			logger.info { "Lori User Perm Check: ${System.currentTimeMillis() - start}" }
		}

		val canBypass = loritta.isOwner(userIdentification.id) || canAccessDashboardViaPermission
		if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true || jdaGuild.ownerId == userIdentification.id)) {
			call.respondText("Você não tem permissão!")
			return
		}

		val isGuildBanned = LorittaUtils.isGuildBanned(loritta, jdaGuild)
		if (isGuildBanned) {
			logger.info { "User ${userIdentification.id} in server ${jdaGuild.idLong} attempted to access guild, but the server is banned!" }
			call.respondText("Servidor está banido de usar a Loritta!")
			return
		}

		// variables["serverConfig"] = legacyServerConfig
		// TODO: Remover isto quando for removido o "server-config-json" do website
		start = System.currentTimeMillis()
		val variables = call.legacyVariables(loritta, locale)
		logger.info { "Legacy Vars Creation: ${System.currentTimeMillis() - start}" }
		variables["guild"] = jdaGuild

		return onGuildAuthenticatedRequest(call, locale, i18nContext, discordAuth, userIdentification, jdaGuild, serverConfig)
	}
}