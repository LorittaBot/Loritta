package net.perfectdreams.loritta.website.routes.api.v1.guilds

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.socket.network.ConfigSectionOpCode
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.LoriWebCode
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.website

class PatchGuildConfigRoute : BaseRoute("/api/v1/guilds/{guildId}/config") {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = call.sessions.get<SampleSession>() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        if (session.discordId == null) {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        val text = call.receiveText()

        website.controller.discord.patchGuildConfigById(
            call.parameters["guildId"]!!,
            ConfigSectionOpCode.GENERAL,
            Constants.JSON_MAPPER.readTree(text),
            session.discordId
        )

        call.respondJson("{}")
    }
}