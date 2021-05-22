package net.perfectdreams.loritta.website.routes.landingpages

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class BrazilianBotLandingPageRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/discord-bot-brasileiro") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
			LorittaWebsite.INSTANCE.pageProvider.render(
				RouteKey.BRAZILIAN_BOT,
				listOf(
					getPathWithoutLocale(call),
					locale
				)
			)
		)
	}
}