package net.perfectdreams.loritta.website.routes.dashboard

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.LocalizedRoute

class DashboardAuthRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/dashboardauth") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {

	}
}