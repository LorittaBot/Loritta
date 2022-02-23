package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.PremiumView

class PremiumRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.PREMIUM) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        call.respondText(
            PremiumView(
                call.request.userTheme,
                showtime.svgIconManager,
                showtime.hashManager,
                locale,
                "/donate"
            ).generateHtml(),
            ContentType.Text.Html
        )
    }
}