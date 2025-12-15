package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.UserBannedView
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession

abstract class RequiresDiscordLoginLocalizedRoute(loritta: LorittaBot, path: String) : LocalizedRoute(loritta, path) {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, session: LorittaUserSession)

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        val session = loritta.dashboardWebServer.getSession(call)
        if (session == null) {
            onUnauthenticatedRequest(call, locale, i18nContext)
            return
        }

		val profile = loritta.getOrCreateLorittaProfile(session.userId)
		val bannedState = profile.getBannedState(loritta)
		if (bannedState != null) {
			call.respondHtml(
				UserBannedView(
					loritta,
					i18nContext,
					locale,
					getPathWithoutLocale(call),
					profile,
					bannedState
				).generateHtml()
			)
			return
		}

		onAuthenticatedRequest(call, locale, i18nContext, session)
	}

	open suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		// redirect to authentication owo
		redirect(LorittaDiscordOAuth2AuthorizeScopeURL(loritta, LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + call.request.path()).toString(), false)
	}
}