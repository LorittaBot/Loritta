package net.perfectdreams.loritta.morenitta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.DailyView

class DailyRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/daily") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		call.respondHtml(
			DailyView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call)
			).generateHtml()
		)
	}
}