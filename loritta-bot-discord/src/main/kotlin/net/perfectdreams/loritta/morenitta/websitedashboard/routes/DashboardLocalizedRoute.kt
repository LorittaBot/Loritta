package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class DashboardLocalizedRoute(val website: LorittaDashboardWebServer, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeId = call.parameters.getOrFail("localeId")

        val matchedI18nContext = website.loritta.languageManager.languageContexts.values.firstOrNull {
            it.get(I18nKeysData.Website.LocalePathId) == localeId
        }

        if (matchedI18nContext == null) {
            // We don't know what locale the user is trying to access!
            // So we will redirect to a valid locale path...
            val validI18nContext = website.getI18nContextFromCall(call)
            call.respondRedirect("/${validI18nContext.get(I18nKeysData.Website.LocalePathId)}/${call.request.uri.removePrefix("/${localeId}/")}", false)
            return
        }

        onLocalizedRequest(call, matchedI18nContext)
    }

    abstract suspend fun onLocalizedRequest(
        call: ApplicationCall,
        i18nContext: I18nContext
    )
}