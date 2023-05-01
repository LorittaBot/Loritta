package net.perfectdreams.loritta.facingworlds.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.perfectdreams.loritta.facingworlds.backend.FacingWorldsBackend
import net.perfectdreams.sequins.ktor.BaseRoute

fun Application.configureRouting(m: FacingWorldsBackend, routes: List<BaseRoute>) {
    routing {
        get("/facing-worlds") {
            call.respondText("This ancient asteroid has been converted to an Arena for the Tournament. It is highly dangerous due to aberrant gravitational properties and, of course, the snipers from the other team.")
        }

        for (route in routes) {
            route.register(this)
        }
    }
}