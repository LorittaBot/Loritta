package net.perfectdreams.loritta.website.utils.extensions

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.Constants

suspend fun ApplicationCall.respondJson(json: String) = this.respondText(json, ContentType.Application.Json)
suspend fun ApplicationCall.respondJson(node: JsonNode) = this.respondText(Constants.JSON_MAPPER.writeValueAsString(node), ContentType.Application.Json)


