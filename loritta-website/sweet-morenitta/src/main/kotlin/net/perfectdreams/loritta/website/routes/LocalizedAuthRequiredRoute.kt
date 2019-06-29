package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.response.respondText
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.utils.deserialize
import net.perfectdreams.loritta.website.utils.identification.SimpleUserIdentification
import net.perfectdreams.loritta.website.utils.website

abstract class LocalizedAuthRequiredRoute(path: String) : LocalizedRoute(path) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val session = call.sessions.get<SampleSession>()
        println("SESSION IS $session")

        if (session?.serializedDiscordAuth == null) {
            call.respondText("Not authenticated yet! ;w;")
            return
        }

        val discordAuth = session.serializedDiscordAuth.deserialize()
        val userIdentification = website.controller.discord.getUserIdentification(call, session, discordAuth)

        onLocalizedAuthRequiredRequest(call, locale, discordAuth, userIdentification)
    }

    abstract suspend fun onLocalizedAuthRequiredRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: SimpleUserIdentification)
}