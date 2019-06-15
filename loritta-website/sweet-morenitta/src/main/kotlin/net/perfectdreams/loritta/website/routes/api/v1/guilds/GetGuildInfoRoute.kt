package net.perfectdreams.loritta.website.routes.api.v1.guilds

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.LoriWebCode
import net.perfectdreams.loritta.website.utils.WebsiteUtils

class GetGuildInfoRoute : BaseRoute("/api/v1/guilds/{guildId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = call.sessions.get<SampleSession>() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        if (session.discordId == null) {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }
    }
}