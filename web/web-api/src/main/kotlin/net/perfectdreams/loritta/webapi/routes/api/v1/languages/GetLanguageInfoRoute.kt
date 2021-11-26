package net.perfectdreams.loritta.webapi.routes.api.v1.languages

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.webapi.LorittaWebAPI
import net.perfectdreams.sequins.ktor.BaseRoute

class GetLanguageInfoRoute(private val m: LorittaWebAPI) : BaseRoute("/api/v1/languages/{languageId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText(
            Json.encodeToString(m.languageManager.getLanguageById(call.parameters.getOrFail("languageId")))
        )
    }
}