package net.perfectdreams.loritta.facingworlds.backend.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.sequins.ktor.BaseRoute

class GetFacingWorldsRoute : BaseRoute("/facing-worlds") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText("This ancient asteroid has been converted to an Arena for the Tournament. It is highly dangerous due to aberrant gravitational properties and, of course, the snipers from the other team.")
    }
}