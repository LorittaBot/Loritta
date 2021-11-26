package net.perfectdreams.loritta.webapi.routes.api.v1

import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.webapi.data.CreateSessionResponse
import net.perfectdreams.sequins.ktor.BaseRoute

class PostCreateSessionRoute : BaseRoute("/api/v1/session") {
    override suspend fun onRequest(call: ApplicationCall) {
        // Creates a session and returns the token to the client
        call.respondText(
            Json.encodeToString(
                CreateSessionResponse("TEMPORARY_TOKEN")
            )
        )
    }
}