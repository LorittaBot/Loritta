package net.perfectdreams.loritta.website.routes.sponsors

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class SponsorsRedirectRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/sponsor/{sponsorSlug}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val sponsorSlug = call.parameters["sponsorSlug"]

		val sponsor = com.mrpowergamerbr.loritta.utils.loritta.sponsors.firstOrNull { it.slug == sponsorSlug } ?: return

		call.respondHtml(
			LorittaWebsite.INSTANCE.pageProvider.render(
				RouteKey.SPONSOR_REDIRECT,
				listOf(
					getPathWithoutLocale(call),
					locale,
					sponsor
				)
			)
		)
	}
}