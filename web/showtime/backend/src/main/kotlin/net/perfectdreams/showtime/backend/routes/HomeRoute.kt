package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.HomeView

class HomeRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.HOME) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            call.respondHtml(
                block = HomeView(
                    showtime,
                    call.request.userTheme,
                    locale,
                    i18nContext,
                    "/"
                ).generateHtml()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}