package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.utils.gson
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute

class GetLocaleRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/locale/{localeId}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeId = call.parameters["localeId"]

		val locale = loritta.localeManager.locales[localeId] ?: loritta.localeManager.locales["default"]!!

		call.respondText(ContentType.Application.Json) { gson.toJson(locale) }
	}
}