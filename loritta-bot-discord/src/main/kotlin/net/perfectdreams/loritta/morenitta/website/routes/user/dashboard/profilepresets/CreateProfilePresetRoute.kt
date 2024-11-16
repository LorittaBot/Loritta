package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.CreateProfilePresetView
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class CreateProfilePresetRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/profile-presets/create") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val result = loritta.transaction {
			val profile = loritta.getLorittaProfile(userIdentification.id.toLong())
			val activeProfileDesignId = profile?.settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
			val activeBackgroundId = profile?.settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID

			return@transaction Result(activeProfileDesignId, activeBackgroundId)
		}

		val view = CreateProfilePresetView(
			loritta.newWebsite!!,
			i18nContext,
			locale,
			getPathWithoutLocale(call),
			loritta.getLegacyLocaleById(locale.id),
			userIdentification,
			UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
			colorTheme,
			result.activeProfileDesignId,
			result.activeBackgroundId
		)

		call.respondHtml(
			view.generateHtml()
		)
	}

	private data class Result(
		val activeProfileDesignId: String,
		val activeBackgroundId: String
	)
}