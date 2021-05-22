package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class SponsorsRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/sponsors") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
			LorittaWebsite.INSTANCE.pageProvider.render(
				RouteKey.SPONSORS,
				listOf(
					getPathWithoutLocale(call),
					locale
				)
			)
		)
	}
}