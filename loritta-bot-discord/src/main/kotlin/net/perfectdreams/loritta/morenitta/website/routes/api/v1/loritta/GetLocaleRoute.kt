package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import net.perfectdreams.loritta.morenitta.utils.gson
import io.ktor.server.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.sequins.ktor.BaseRoute

class GetLocaleRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/loritta/locale/{localeId}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeId = call.parameters["localeId"]

		// To avoid malicious users bypassing the cache by using random locale keys, we will validate if the locale exists or not
		if (!loritta.localeManager.locales.containsKey(localeId)) {
			call.respondText("", ContentType.Application.Json, status = HttpStatusCode.NotFound)
			return
		}

		val locale = loritta.localeManager.locales[localeId]

		call.respondText(ContentType.Application.Json) { gson.toJson(locale) }
	}
}