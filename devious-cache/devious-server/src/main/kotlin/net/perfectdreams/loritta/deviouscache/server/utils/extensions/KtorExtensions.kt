package net.perfectdreams.loritta.deviouscache.server.utils.extensions

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse

suspend inline fun <reified T> ApplicationCall.respondJson(
    serializableObject: T,
    status: HttpStatusCode? = null,
    noinline configure: OutgoingContent.() -> Unit = {}
) = respondText(Json.encodeToString(serializableObject), ContentType.Application.Json, status, configure)

suspend inline fun ApplicationCall.respondNotFound() = respondJson(Json.encodeToString(NotFoundResponse), status = HttpStatusCode.NotFound)