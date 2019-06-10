package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.utils.website

class QueryCanaryUserRoute : LocalizedRoute("/query-canary") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val test = website.controller.discord.retrieveUserById("395935916952256523")

        call.respondText(test?.name ?: "Unknown User!")
    }
}