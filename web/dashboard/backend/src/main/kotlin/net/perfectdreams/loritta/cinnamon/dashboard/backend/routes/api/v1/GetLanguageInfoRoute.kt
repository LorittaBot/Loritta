package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.BaseRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.respondJson
import org.apache.commons.codec.digest.DigestUtils

class GetLanguageInfoRoute(private val m: LorittaDashboardBackend) : BaseRoute("/api/v1/languages/{languageId}") {
    override suspend fun onRequest(call: ApplicationCall) {
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