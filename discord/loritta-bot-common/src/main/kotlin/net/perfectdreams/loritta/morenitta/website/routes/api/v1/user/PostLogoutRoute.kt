package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class PostLogoutRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/users/@me/logout") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.sessions.clear<LorittaJsonWebSession>()
        call.respondJson(jsonObject())
    }
}