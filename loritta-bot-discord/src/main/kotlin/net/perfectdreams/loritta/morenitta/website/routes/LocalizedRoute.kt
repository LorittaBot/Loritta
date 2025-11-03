package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class LocalizedRoute(val loritta: LorittaBot, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    open val isMainClusterOnlyRoute = false

    override suspend fun onRequest(call: ApplicationCall) {
        if (isMainClusterOnlyRoute && !loritta.isMainInstance)
        // If this is a main cluster only route, we are going to redirect to Loritta's main website
            redirect(loritta.config.loritta.website.url.removeSuffix("/") + call.request.path(), true)

        val localeIdFromPath = call.parameters["localeId"]

        // Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
        val locale = loritta.localeManager.locales.values.firstOrNull { it.path == localeIdFromPath }
        if (locale != null) {
            val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(locale.id)

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