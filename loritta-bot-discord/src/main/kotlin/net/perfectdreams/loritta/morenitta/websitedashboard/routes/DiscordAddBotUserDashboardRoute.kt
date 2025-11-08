package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.request.header
import io.ktor.server.response.respondRedirect
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils
import net.perfectdreams.sequins.ktor.BaseRoute
import java.util.Base64

class DiscordAddBotUserDashboardRoute(val website: LorittaDashboardWebServer) : BaseRoute("/discord/add") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val guildId = call.request.queryParameters["guildId"]?.toLong()
        val source = call.request.queryParameters["source"]
        val medium = call.request.queryParameters["medium"]
        val campaign = call.request.queryParameters["campaign"]
        val content = call.request.queryParameters["content"]
        val referrer = call.request.header(HttpHeaders.Referrer)

        val state = AuthenticationState(
            source,
            medium,
            campaign,
            content,
            referrer,
            null
        )

        val stateAsBase64 = AuthenticationStateUtils.createStateAsBase64(state, website.loritta)

        call.respondRedirect(
            net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL(
                website.loritta,
                guildId,
                stateAsBase64
            ).toString(),
            false
        )
    }
}