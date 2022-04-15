package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.HttpRedirectException

class LegacyCommandsRedirectRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.COMMANDS_REDIRECT) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        throw HttpRedirectException(call.request.path().replace("/commands", "/commands/legacy"))
    }
}