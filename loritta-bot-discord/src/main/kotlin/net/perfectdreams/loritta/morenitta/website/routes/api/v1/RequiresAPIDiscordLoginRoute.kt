package net.perfectdreams.loritta.morenitta.website.routes.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

abstract class RequiresAPIDiscordLoginRoute(val loritta: LorittaBot, path: String) : BaseRoute(path) {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, session: UserSession)

	override suspend fun onRequest(call: ApplicationCall) {
        val session = loritta.dashboardWebServer.getSession(call)

		if (session == null)
			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.UNAUTHORIZED,
							"Invalid Discord Authorization"
					)
			)

		val profile = loritta.getOrCreateLorittaProfile(session.userId)
		val bannedState = profile.getBannedState(loritta)

		if (bannedState != null)
			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.BANNED,
							"You are Loritta Banned!"
					)
			)

		onAuthenticatedRequest(call, session)
	}
}