package net.perfectdreams.loritta.cinnamon.dashboard.backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.BaseRoute
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.switchtwitch.SwitchTwitchAPI
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class TwitchCallbackRoute(val m: LorittaDashboardBackend) : BaseRoute("/twitch-callback") {
    override suspend fun onRequest(call: ApplicationCall) {
        val api = SwitchTwitchAPI.fromAuthCode(
            m.config.twitch.clientId,
            m.config.twitch.clientSecret,
            call.request.queryParameters["code"]!!,
            m.config.twitch.redirectUri
        )

        val response = api.getSelfUserInfo()

        m.pudding.transaction {
            AuthorizedTwitchAccounts.upsert(AuthorizedTwitchAccounts.userId) {
                it[AuthorizedTwitchAccounts.userId] = response.id
                it[AuthorizedTwitchAccounts.authorizedAt] = Instant.now()
            }
        }

        call.respondText("<script>window.opener.postMessage(\"${response.id}\"); window.close();</script>", ContentType.Text.Html)
        return
    }
}