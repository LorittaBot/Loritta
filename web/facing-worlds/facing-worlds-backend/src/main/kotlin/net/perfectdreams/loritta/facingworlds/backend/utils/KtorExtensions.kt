package net.perfectdreams.loritta.facingworlds.backend.utils

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondJson(
    text: String,
    status: HttpStatusCode? = null,
    configure: OutgoingContent.() -> Unit = {}
) = respondText(text, ContentType.Application.Json, status, configure)
