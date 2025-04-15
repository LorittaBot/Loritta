package net.perfectdreams.loritta.website.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.userTheme
import net.perfectdreams.loritta.website.backend.views.DashboardView

class DashboardRoute(val showtime: LorittaWebsiteBackend) : LocalizedRoute(showtime, RoutePath.DASHBOARD) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        call.respondHtml(
            block = DashboardView(
                showtime,
                call.request.userTheme,
                locale,
                i18nContext,
                "/dashboard"
            ).generateHtml()
        )
    }
}