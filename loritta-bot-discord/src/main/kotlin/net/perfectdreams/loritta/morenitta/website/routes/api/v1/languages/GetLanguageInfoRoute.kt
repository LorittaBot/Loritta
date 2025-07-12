package net.perfectdreams.loritta.morenitta.website.routes.api.v1.languages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import org.apache.commons.codec.digest.DigestUtils

class GetLanguageInfoRoute(private val m: LorittaBot) : BaseRoute("/api/v1/languages/{languageId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val languageId = call.parameters.getOrFail("languageId")

        // To avoid malicious users bypassing the cache by using random locale keys, we will validate if the locale exists or not
        if (!m.languageManager.languages.containsKey(languageId)) {
            call.respondText("", ContentType.Application.Json, status = HttpStatusCode.NotFound)
            return
        }

        val language = m.languageManager.getLanguageById(call.parameters.getOrFail("languageId"))

        val dataAsJson = Json.encodeToString(language)

        // Naive Etag implementation: Check if the data hashCode changed or not, if it hasn't, we don't need to send the entire payload again
        val eTagKey = DigestUtils.sha256Hex(dataAsJson)

        if (call.request.header("If-None-Match") == "W/\"$eTagKey\"") {
            call.respond(HttpStatusCode.NotModified)
            return
        }

        call.response.header("ETag", "W/\"$eTagKey\"")

        call.respondJson(dataAsJson)
    }
}