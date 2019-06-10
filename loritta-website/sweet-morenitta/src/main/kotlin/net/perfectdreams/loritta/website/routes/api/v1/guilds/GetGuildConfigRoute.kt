package net.perfectdreams.loritta.website.routes.api.v1.guilds

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.socket.network.ConfigSectionOpCode
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.LoriWebCode
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.website

class GetGuildConfigRoute : BaseRoute("/api/v1/guilds/{guildId}/config/{sections?}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = call.sessions.get<SampleSession>() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        if (session.discordId == null) {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        val sectionsAsString = call.parameters["sections"] ?: ConfigSectionOpCode.GENERAL.toString()
        val sections = sectionsAsString.split(",")

        val guildId = call.parameters["guildId"]!!
        val guildConfig = website.controller.discord.retrieveGuildConfigById(
            sections,
            guildId.toLong(),
            session.discordId.toLong()
        ) ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNKNOWN_SOMETHING, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        call.respondJson(
            guildConfig
        )
    }
}