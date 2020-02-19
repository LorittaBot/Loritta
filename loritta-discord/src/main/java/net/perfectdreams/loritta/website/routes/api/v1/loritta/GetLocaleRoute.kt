package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.set
import net.perfectdreams.loritta.website.routes.BaseRoute

class GetLocaleRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/locale/{localeId}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val localeId = call.parameters["localeId"]

		val locale = loritta.locales[localeId] ?: loritta.locales["default"]!!

		val localeEntries = objectNode()
		locale.localeEntries.forEach {
			val value = it.value

			if (value is String) {
				localeEntries[it.key] = value
			} else if (value is List<*>) {
				localeEntries[it.key] = "list::${value.joinToString("\n")}"
			}
		}

		val node = objectNode(
				"id" to locale.id,
				"path" to locale.path,
				"localeEntries" to localeEntries
		)

		call.respondText(ContentType.Application.Json) { Constants.JSON_MAPPER.writeValueAsString(node) }
	}
}