package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.ContactView

class ContactRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.CONTACT) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        call.respondHtml(
            block = ContactView(
                showtime,
                call.request.userTheme,
                locale,
                i18nContext,
                "/contact"
            ).generateHtml()
        )
    }
}