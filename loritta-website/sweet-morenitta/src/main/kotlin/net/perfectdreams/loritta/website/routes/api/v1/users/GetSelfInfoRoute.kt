package net.perfectdreams.loritta.website.routes.api.v1.users

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.LoriWebCode
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.deserialize

class GetSelfInfoRoute : BaseRoute("/api/v1/users/@me") {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = call.sessions.get<SampleSession>() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        val userIdentification = session.serializedDiscordAuth?.deserialize()?.getUserIdentification() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
            Constants.JSON_MAPPER.writeValueAsString(userIdentification)
        }
    }
}