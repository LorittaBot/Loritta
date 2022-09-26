package net.perfectdreams.loritta.legacy.website.routes.sponsors

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.utils.Sponsor
import net.perfectdreams.loritta.legacy.website.LorittaWebsite
import net.perfectdreams.loritta.legacy.website.routes.LocalizedRoute
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.legacy.website.views.SponsorRedirectView

class SponsorsRedirectRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/sponsor/{sponsorSlug}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val sponsorSlug = call.parameters["sponsorSlug"]

		val sponsor = net.perfectdreams.loritta.legacy.utils.loritta.sponsors.firstOrNull { it.slug == sponsorSlug } ?: return

		call.respondHtml(
			SponsorRedirectView(
				locale,
				getPathWithoutLocale(call),
				sponsor
			).generateHtml()
		)
	}
}