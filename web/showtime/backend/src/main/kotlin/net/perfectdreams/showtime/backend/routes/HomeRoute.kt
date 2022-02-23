package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.HomeView

class HomeRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.HOME) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        try {
            call.respondText(
                HomeView(
                    call.request.userTheme,
                    showtime.svgIconManager,
                    showtime.hashManager,
                    locale,
                    "/"
                ).generateHtml(),
                ContentType.Text.Html
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}