package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.LorittaRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse

suspend inline fun <reified T> ApplicationCall.receiveAndDecodeJson() = Json.decodeFromString<T>(receiveText())

suspend inline fun <reified T : LorittaRequest> ApplicationCall.receiveAndDecodeRequest(): T
        = Json.decodeFromString<LorittaRequest>(receiveText()) as T

suspend fun ApplicationCall.respondJson(
    text: String,
    status: HttpStatusCode? = null,
    configure: OutgoingContent.() -> Unit = {}
) = respondText(text, ContentType.Application.Json, status, configure)

suspend inline fun <reified T> ApplicationCall.respondJson(
    serializableObject: T,
    status: HttpStatusCode? = null,
    noinline configure: OutgoingContent.() -> Unit = {}
) = respondText(Json.encodeToString(serializableObject), ContentType.Application.Json, status, configure)

suspend inline fun <reified T : LorittaResponse> ApplicationCall.respondLoritta(
    serializableObject: T,
    status: HttpStatusCode? = null,
    noinline configure: OutgoingContent.() -> Unit = {}
) = respondText(Json.encodeToString<LorittaResponse>(serializableObject), ContentType.Application.Json, status, configure)


var ApplicationCall.lorittaSession: LorittaJsonWebSession
    get() {
        return this.sessions.get() ?: LorittaJsonWebSession.empty()
    }
    set(value) {
        this.sessions.set(value)
    }