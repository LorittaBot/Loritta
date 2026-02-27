package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect

class DonateRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/donate") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		redirect(loritta.config.loritta.dashboard.url.removeSuffix("/") + "/${locale.path}/premium", permanent = false)
	}
}
