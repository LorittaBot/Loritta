package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class DashboardLocalizedRoute(val website: LorittaDashboardWebServer, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        onLocalizedRequest(call, website.loritta.languageManager.defaultI18nContext)
    }

    abstract suspend fun onLocalizedRequest(
        call: ApplicationCall,
        i18nContext: I18nContext
    )
}