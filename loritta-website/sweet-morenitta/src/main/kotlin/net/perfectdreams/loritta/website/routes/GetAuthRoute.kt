package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.userAgent
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.serialize
import org.w3c.dom.Element
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class GetAuthRoute : BaseRoute("/auth") {
    override suspend fun onRequest(call: ApplicationCall) {
        if (call.request.userAgent() == Constants.DISCORD_CRAWLER_USER_AGENT) {
            call.respondText(
                    WebsiteUtils.getDiscordCrawlerAuthenticationPage(),
                    ContentType.Text.Html
            )
            return
        }

        val code = call.parameters["code"]
        if (code == null) {
            call.respondRedirect("https://discordapp.com/oauth2/authorize?redirect_uri=https://spicy.loritta.website%2Fauth&scope=identify%20email%20connections%20guilds&response_type=code&client_id=395935916952256523&state=eyJyZWRpcmVjdFVybCI6Imh0dHBzOi8vY2FuYXJ5Lmxvcml0dGEud2Vic2l0ZS9ici9kYXNoYm9hcmQifQ%3D%3D")
        } else {
            println("Code: $code")

            val discordAuth = TemmieDiscordAuth(
                    LorittaWebsite.INSTANCE.config.clientId,
                    LorittaWebsite.INSTANCE.config.clientToken,
                    code,
                    "https://spicy.loritta.website/auth",
                    listOf("identify", "email", "connections", "guilds")
            )

            discordAuth.doTokenExchange()

            val identification = discordAuth.getUserIdentification()

            val test = ScriptingUtils.evaluateTemplate<Any>(
                    File(
                            "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/auth_popup.kts"
                    ),
                    mapOf(
                            "document" to "Document",
                            "locale" to "BaseLocale",
                            "userIdentification" to "String"
                    )
            )

            val document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument()

            val element = test::class.members.first { it.name == "generateHtml" }.call(
                    test,
                    document,
                    LorittaWebsite.INSTANCE.locales["default"],
                    Constants.JSON_MAPPER.writeValueAsString(identification)
            ) as Element

            document.appendChild(element)

            val session = call.sessions.get<SampleSession>() ?: SampleSession(
                    uniqueId = UUID.randomUUID(),
                    discordId = identification.id,
                    serializedDiscordAuth = discordAuth.serialize()
            )

            call.sessions.set(session.copy(serializedDiscordAuth = discordAuth.serialize()))

            call.respondText(LorittaWebsite.INSTANCE.transformToString(document), ContentType.Text.Html)
        }
    }
}