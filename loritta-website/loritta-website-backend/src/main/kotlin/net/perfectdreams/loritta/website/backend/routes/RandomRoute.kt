package net.perfectdreams.loritta.website.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import java.util.*

class RandomRoute(val showtime: LorittaWebsiteBackend) : LocalizedRoute(showtime, RoutePath.RANDOM) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        if (call.request.header("Bliss-Triggered-By-Id") == "click-to-random")
            call.response.header("Bliss-Redirect", "https://google.com/")

        call.respondHtml {
            body {
                div {
                    text(UUID.randomUUID().toString())
                }

                div {
                    button {
                        attributes["bliss-get"] = "/br/"
                        attributes["bliss-swaps"] = "#jumbotron -> #swap-me-pls"

                        text("hewwo!")
                    }

                    div {
                        id = "swap-me-pls"
                    }
                }
            }
        }
    }
}