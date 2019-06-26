package net.perfectdreams.loritta.website.routes.dashboard

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedAuthRequiredRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.identification.SimpleUserIdentification
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class DashboardRoute : LocalizedAuthRequiredRoute("/dashboard") {
    override suspend fun onLocalizedAuthRequiredRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: SimpleUserIdentification) {
        val test = ScriptingUtils.evaluateTemplate<Any>(
            File(
                "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/dashboard.kts"
            ),
            mapOf(
                "document" to "Document",
                "websiteUrl" to "String",
                "locale" to "BaseLocale"
            )
        )

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .newDocument()

        val element = test::class.members.first { it.name == "generateHtml" }.call(
            test,
            document,
            LorittaWebsite.INSTANCE.config.websiteUrl,
            LorittaWebsite.INSTANCE.locales["default"]
        ) as Element

        document.appendChild(element)

        call.respondText(LorittaWebsite.INSTANCE.transformToString(document), ContentType.Text.Html)
    }
}