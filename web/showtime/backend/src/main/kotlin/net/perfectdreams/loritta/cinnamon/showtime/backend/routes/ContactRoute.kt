package net.perfectdreams.loritta.cinnamon.showtime.backend.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.userTheme
import net.perfectdreams.loritta.cinnamon.showtime.backend.views.ContactView

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