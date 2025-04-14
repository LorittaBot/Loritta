package net.perfectdreams.loritta.website.backend.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

abstract class LocalizedRoute(val loritta: LorittaWebsiteBackend, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeIdFromPath = call.parameters["localeId"]

        // Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
        val locale = loritta.locales.values.firstOrNull { it.path == localeIdFromPath }

        if (locale != null) {
            // woo hacks
            val i18nContext = when (locale.id) {
                "default" -> loritta.languageManager.getI18nContextById("pt")
                "en-us" -> loritta.languageManager.getI18nContextById("en")
                else -> loritta.languageManager.getI18nContextById(loritta.languageManager.defaultLanguageId)
            }

            return onLocalizedRequest(
                call,
                locale,
                i18nContext
            )
        }
    }

    abstract suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext)

    fun getPathWithoutLocale(call: ApplicationCall) = call.request.path().split("/").drop(2).joinToString("/")
}