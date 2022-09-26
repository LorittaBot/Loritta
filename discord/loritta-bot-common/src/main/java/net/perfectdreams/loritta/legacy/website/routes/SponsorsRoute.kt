package net.perfectdreams.loritta.legacy.website.routes

import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.legacy.website.views.SponsorsView

class SponsorsRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/sponsors") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
			SponsorsView(
				locale,
				getPathWithoutLocale(call),
			).generateHtml()
		)
	}
}