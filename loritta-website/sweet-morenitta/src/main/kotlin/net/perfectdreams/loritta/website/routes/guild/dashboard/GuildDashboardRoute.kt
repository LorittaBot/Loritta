package net.perfectdreams.loritta.website.routes.guild.dashboard

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.identification.SimpleUserIdentification
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class GuildDashboardRoute : LocalizedGuildAuthRequiredRoute("/dashboard") {
    override suspend fun onLocalizedGuildAuthRequiredRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: SimpleUserIdentification,
        guild: Guild
    ) {
        val test = ScriptingUtils.evaluateTemplate<Any>(
            File(
                "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/guild/dashboard/general.kts"
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