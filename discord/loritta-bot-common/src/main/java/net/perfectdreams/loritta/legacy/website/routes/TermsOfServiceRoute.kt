package net.perfectdreams.loritta.legacy.website.routes

import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml

class TermsOfServiceRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/privacy") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val variables = call.legacyVariables(locale)
		call.respondHtml(evaluate("terms_of_service.html", variables))
	}
}