package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.views.BlogView
import javax.xml.parsers.DocumentBuilderFactory

class BlogRoute : LocalizedRoute("/blog") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .newDocument()

        val element = BlogView(document).generate(locale)
        document.appendChild(element)

        call.respondText(LorittaWebsite.INSTANCE.transformToString(document), ContentType.Text.Html)
    }
}