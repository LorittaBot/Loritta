package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.ApplicationCall
import kotlinx.html.body
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.switchtwitch.SwitchTwitchAPI
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class TwitchAccountCallbackRoute(val website: LorittaDashboardWebServer) : BaseRoute("/twitch-callback") {
    override suspend fun onRequest(call: ApplicationCall) {
        val api = SwitchTwitchAPI.fromAuthCode(
            website.loritta.config.loritta.twitch.clientId,
            website.loritta.config.loritta.twitch.clientSecret,
            call.request.queryParameters["code"]!!,
            website.loritta.config.loritta.twitch.redirectUri
        )

        val response = api.getSelfUserInfo()

        website.loritta.pudding.transaction {
            AuthorizedTwitchAccounts.upsert(AuthorizedTwitchAccounts.userId) {
                it[AuthorizedTwitchAccounts.userId] = response.id
                it[AuthorizedTwitchAccounts.authorizedAt] = Instant.now()
            }
        }

        call.respondHtml(
            createHTML()
                .body {
                    script {
                        // We can't access the origin here because it is restricted, so that's why we do "*"!!!
                        unsafe {
                            raw("""
								window.opener.postMessage("${response.id}", "*");
								window.close();
							""".trimIndent())
                        }
                    }
                }
        )
    }
}