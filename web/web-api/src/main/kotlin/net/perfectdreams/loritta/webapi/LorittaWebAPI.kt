package net.perfectdreams.loritta.webapi

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.webapi.plugins.configureRouting
import net.perfectdreams.loritta.webapi.routes.api.v1.PostCreateSessionRoute
import net.perfectdreams.loritta.webapi.routes.api.v1.languages.GetLanguageInfoRoute
import net.perfectdreams.loritta.webapi.routes.api.v1.users.GetCurrentUserGuildsRoute

class LorittaWebAPI(val languageManager: LanguageManager) {
    val routes = listOf(
        PostCreateSessionRoute(),
        GetCurrentUserGuildsRoute(),
        GetLanguageInfoRoute(this)
    )

    fun start() {
        embeddedServer(Netty, port = 8000) {
            install(CORS) {
                anyHost()
            }

            routing {
                get("/") {
                    call.respondText("Hello, world!")
                }

                configureRouting(routes)
            }
        }.start(wait = true)
    }
}