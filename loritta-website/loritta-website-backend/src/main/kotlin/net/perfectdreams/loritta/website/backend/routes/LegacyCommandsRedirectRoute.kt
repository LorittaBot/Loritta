package net.perfectdreams.loritta.website.backend.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.HttpRedirectException

class LegacyCommandsRedirectRoute(val showtime: LorittaWebsiteBackend) : LocalizedRoute(showtime, RoutePath.COMMANDS_REDIRECT) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        throw HttpRedirectException(call.request.path().replace("/commands", "/commands/legacy"))
    }
}