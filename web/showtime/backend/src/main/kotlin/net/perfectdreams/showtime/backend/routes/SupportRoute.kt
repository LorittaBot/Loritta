package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.SupportView

class SupportRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.SUPPORT) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        call.respondText(
            SupportView(
                call.request.userTheme,
                showtime.svgIconManager,
                showtime.hashManager,
                locale,
                "/support"
            ).generateHtml(),
            ContentType.Text.Html
        )
    }
}