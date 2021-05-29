package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class HomeRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
				LorittaWebsite.INSTANCE.pageProvider.render(
						RouteKey.HOME,
						listOf(
								getPathWithoutLocale(call),
								locale
						)
				)
		)
	}
}