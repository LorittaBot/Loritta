package net.perfectdreams.loritta.legacy.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.legacy.website.views.DailyView

class DailyRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/daily") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
			DailyView(
				locale,
				getPathWithoutLocale(call)
			).generateHtml()
		)
	}
}