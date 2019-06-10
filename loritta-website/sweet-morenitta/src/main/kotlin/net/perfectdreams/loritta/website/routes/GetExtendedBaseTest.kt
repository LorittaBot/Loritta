package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class GetExtendedBaseTest : LocalizedRoute("/extended") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val test = ScriptingUtils.evaluateTemplate<Any>(
            File(
                "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/extended_base_test.kts"
            ),
            mapOf(
                "document" to "Document",
                "locale" to "BaseLocale",
                "text" to "String"
            )
        )

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .newDocument()

        val element = test::class.members.first { it.name == "generateHtml" }.call(
            test,
            document,
            LorittaWebsite.INSTANCE.locales["default"],
            "owo whats this???"
        ) as Element

        document.appendChild(element)

        call.respondText(LorittaWebsite.INSTANCE.transformToString(document), ContentType.Text.Html)
    }
}