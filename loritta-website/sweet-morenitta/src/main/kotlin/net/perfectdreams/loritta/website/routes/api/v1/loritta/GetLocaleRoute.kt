package net.perfectdreams.loritta.website.routes.api.v1.loritta

import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.set
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.website

class GetLocaleRoute : BaseRoute("/api/v1/loritta/locale/{localeId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeId = call.parameters["localeId"] ?: "default"

        val locale = website.locales[localeId] ?: website.locales["default"]!!

        val localeEntries = objectNode()
        locale.localeEntries.forEach {
            val value = it.value

            if (value is String) {
                localeEntries[it.key] = value
            }
        }

        val node = objectNode(
            "id" to locale.id,
            "path" to locale.path,
            "localeEntries" to localeEntries
        )

        call.respondJson(node)
    }
}