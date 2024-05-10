package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.evaluate
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleProfileDashboardRawHtmlView
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class AvailableBundlesRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/bundles") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(loritta, locale)
		variables["saveType"] = "bundles"

		call.respondHtml(
			LegacyPebbleProfileDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				"Painel de Controle",
				evaluate("profile_dashboard_bundles.html", variables),
				"bundles"
			).generateHtml()
		)
	}
}