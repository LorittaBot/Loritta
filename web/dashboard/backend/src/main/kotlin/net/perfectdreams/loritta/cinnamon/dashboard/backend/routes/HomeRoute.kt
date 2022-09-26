package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend

class HomeRoute(m: LorittaDashboardBackend) : LocalizedRoute(m, "/") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        call.respondText("SpicyMorenitta")
    }
}