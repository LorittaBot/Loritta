package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class LocalizedRoute(val m: LorittaDashboardBackend, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeIdFromPath = call.parameters["localeId"]

        val locale = m.languageManager.languageContexts.values.firstOrNull { it.language.textBundle.strings[I18nKeys.Website.Dashboard.LocalePathId.key] == localeIdFromPath }

        if (locale != null) {
            return onLocalizedRequest(
                call,
                locale
            )
        }
    }

    abstract suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext)

    fun getPathWithoutLocale(call: ApplicationCall) = call.request.path().split("/").drop(2).joinToString("/")
}