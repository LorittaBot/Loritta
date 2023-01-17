package net.perfectdreams.loritta.morenitta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView

class TermsOfServiceRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/privacy") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val variables = call.legacyVariables(loritta, locale)
		call.respondHtml(
			LegacyPebbleRawHtmlView(
				loritta,
				locale,
				getPathWithoutLocale(call),
				"Termos de Serviço",
				evaluate("terms_of_service.html", variables)
			).generateHtml()
		)
	}
}