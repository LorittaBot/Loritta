package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.evaluate
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.LegacyPebbleProfileDashboardRawHtmlView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class AllBackgroundsListRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/all-backgrounds") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "background_list_debug"

		call.respondHtml(
			LegacyPebbleProfileDashboardRawHtmlView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				"Painel de Controle",
				evaluate("profile_dashboard_backgrounds_list.html", variables),
				"background_list_debug"
			).generateHtml()
		)
	}
}