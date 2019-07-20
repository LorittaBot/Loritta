package net.perfectdreams.loritta.website.routes.fanarts

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import java.io.File

class FanArtsRoute : LocalizedRoute("/fanarts") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val html = ScriptingUtils.evaluateWebPageFromTemplate(
                File(
                        "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_arts.kts"
                ),
                mapOf(
                        "path" to call.request.path().split("/").drop(2).joinToString("/"),
                        "websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
                        "locale" to locale
                )
        )

        call.respondText(html, ContentType.Text.Html)
    }
}