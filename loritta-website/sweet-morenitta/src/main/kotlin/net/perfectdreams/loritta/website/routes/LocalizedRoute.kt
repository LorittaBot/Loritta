package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite

abstract class LocalizedRoute(val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeIdFromPath = call.parameters["localeId"]

        // Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
        val locale = LorittaWebsite.INSTANCE.locales.values.firstOrNull { it.path == localeIdFromPath }
            ?: LorittaWebsite.INSTANCE.locales.getValue("default")

        return onLocalizedRequest(
            call,
            locale
        )
    }

    abstract suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale)
}